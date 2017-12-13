@(user:User)
@import helper._

var chartdata = null ; 
var chart = null;
var chartoptions = null;

setupJobChart();


var jobs = []


function addJob() {
	$('#addJobModal').modal('show');
}

function setupJobChart() {
	google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(drawChart);

    function drawChart() {
      chartoptions = {
        title: 'How busy am I?',
        vAxis: {title: 'Accumulated Rating'},
       legend: { position: 'top' , maxLines: 3 },
        lineWidth: 0,
    	connectSteps: false,
    	vAxis: {
          minValue: 0,
          ticks: [0, 0.20,0.40,0.60,0.80,1 ]
        },
        colors: ['#0000FF',
        	'#3366CC', '#DC3912', '#FF9900', '#109618', '#990099',
        	'#3B3EAC', '#0099C6', '#DD4477', '#66AA00', '#B82E2E',
        	'#316395', '#994499', '#22AA99', '#AAAA11', '#6633CC',
        	'#E67300', '#8B0707', '#329262', '#5574A6', '#3B3EAC'],
        height: 500,
        isStacked: true,
        animation:{
            duration: 0,
            easing: 'out',
          },
      };

      @if( user != null ) {
      chart = new google.visualization.SteppedAreaChart(document.getElementById('chart_div'));
      }
      
      console.log("CHART ready");
      doJobChart();
    }   
}

function doJobChart() {
	if ( chart != null && jobs.length > 0 ){ 
		var day = 24*60*60*1000
		var projectDays=7
		var today = new Date().getTime() ;
		var jobsFromToday = []
		for ( i=0 ; i < jobs.length ; i=i+1) {
			var j = jobs[i];
			if ( j.end_date_seconds > today ) {
				jobsFromToday.push(j)
				console.log("qualifies today "+j);
			} else {
				console.log("reject "+today+" start="+j.start_date_seconds+" end="+j.end_date_seconds);
			}
		}

		chartdata =  new google.visualization.DataTable();

		chartdata.addColumn('string', 'Date');
		chartdata.addColumn('number', 'Free');

		for( i=0 ; i < jobsFromToday.length ; i=i+1 ) {
			    var j = jobsFromToday[i];
				chartdata.addColumn('number', j.description);
		}

		for( d = today  ; d < today+day*projectDays ; d = d + day) {
			var row = [  ];
			var free = 1
			for( i=0 ; i < jobsFromToday.length ; i=i+1 ) {
			    var j = jobsFromToday[i];
			    if ( j.end_date_seconds > d-day && j.start_date_seconds < d ) {
			    	row.push(j.ideal_daily_effort)
			    	free=free - j.ideal_daily_effort
			    } else {
			    	row.push(0)
			    }
			}
			row.unshift(free)
			var dd = "" + new Date(d)
			row.unshift( dd.split(" ")[0] )
			console.log("ROW ["+row+"]");

			chartdata.addRow(row);
		}
		
		chart.draw(chartdata, chartoptions);
		console.log("DRAW CHART");
	} else  {
		console.log("CHART not ready");
	}
	
}

function loadJobsData() {

	$("#jobs").empty();
	$("#jobs").append('<div class="loader"></div>')
	
	$.getJSON(			
			"api/jobs", 
			function(data) {
				$("#jobs").empty();

				var accordion=0
				jobs = []
				$.each(	data,function(key, j) {
					console.log(j)
					jobs.push(j)
					var head = '<tr>'
					var newone = 
						'<td><a href="#updateJobModal" class="btn btn-default btn-block" onClick="selectJob('+accordion+')" data-id="jobrow'+accordion+'">' + j.description + '</a></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'required_effort\',0)" class="form-control numb" type="number"  min="0" max="1" step="0.25" value="' + j.required_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'actual_effort\',0)" class="form-control numb" type="number"  min="0" max="1" step="0.25" value="' + j.actual_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'ideal_daily_effort\',0)" class="form-control numb" type="number"  min="0" max="1" step="0.25" value="' + j.ideal_daily_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'estimate_left_effort\',0)" class="form-control numb" type="number"  min="0" max="1" step="0.25" value="' + j.estimate_left_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'start_date\',1)" class="form-control " type="date"   value="' + j.start_date + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'end_date\',1)" class="form-control " type="date"   value="' + j.end_date + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'priority\',0)" class="form-control " type="number" min="0" max="99" value="' + j.priority + '"/></td>'+
						'<td><button type="button" onClick="deleteJobEntry('+j.id+')" class="btn btn-danger">-</button></td>'
						var tail = '</tr>'

					$("#jobs").append( head + newone +tail ); 

					accordion=accordion+1
				});
				doJobChart();


			}).fail(function(response) {
				$("#timeentries").empty();
				if ( response.status == 401 ) {
					$("#timeentries").append('<br><br><p align="right"><b>not logged in</b></p>')
				} else {
					$("#timeentries").append('<br><br><p align="right"><b>' + response.statusText + ":"+ response.status + '</b></p>')
				}
				console.log("error " + response.statusText + ":"+ response.status+" - " +response.responseText)
			})
}


var error = null;

function updateValue(comp,row,field,t) {
	if ( t == 0 ) {
		var newValue = parseFloat(comp.value)
	} else {
		var inputDate = new Date(comp.value);
		if ( inputDate == "Invalid Date") return;
		var newValue = comp.value
	}
	console.log("field "+field+" ["+newValue+"]")

	for(key in jobs[row]) {
		if(key == field && jobs[row].hasOwnProperty(key)) {
			var value = jobs[row][key];
			jobs[row][key]=newValue;
		}
	}
	var csrf = $('input[name=csrfToken]').val()
	var json = JSON.stringify(jobs[row])
	console.log("PUT "+json);
	$.ajax({
		type : "PUT",
		headers: {
			'Csrf-Token':csrf
		} ,
		contentType : "application/json; charset=utf-8",
		data : json,
		url : "/api/jobs",
		success : function(data) {
			console.log("SAVED "+JSON.stringify(data));
		},
		error : function(response) {
			alert("error " + response.statusText + ":"+ response.status+" - " +response.responseText)
		}
	});
	doJobChart();
}

function deleteJobEntry(id) {
	var user = $('#user').val()

	var csrf = $('input[name=csrfToken]').val()

	$.ajax({
		type : "DELETE",
		headers: {
			'Csrf-Token':csrf
		} ,
		url : "/api/jobs?id="+id,
		success : function(data) {
			loadJobsData();
		},
		error : function(response) {
			alert("error " + response.statusText + ":"+ response.status+" - " +response.responseText)
		}
	});
}

function deleteEntry(id) {
	var user = $('#user').val()

	var csrf = $('input[name=csrfToken]').val()

	$.ajax({
		type : "DELETE",
		headers: {
			'Csrf-Token':csrf
		} ,
		url : "/api/timeentry?id="+id,
		success : function(data) {
			loadTimeEntryData();
		},
		error : function(response) {
			alert("error " + response.statusText + ":"+ response.status+" - " +response.responseText)
		}
	});
}

function selectJob(j) {
	$('#updateJobModal').data('jobid',j);
	$('#updateJobModal').modal('show')
}

function loadJavascript(){


	function doPostPut(e,id) {
		var user = $('#user').val()

		var e = $("#task_id");
		var csrf = $('input[name=csrfToken]').val()

		var s = ["description","start_date","end_date"]
		var n = ["required_effort","actual_effort","ideal_daily_effort","estimate_left_effort","priority"]

		var json = '{ "id":'+id+' '
		var useType = "PUT"
			if ( id == 0 ) {
				useType = "POST"
					s = ["post_description","post_start_date","post_end_date"]
				n = ["post_required_effort","post_actual_effort","post_ideal_daily_effort","post_estimate_left_effort","post_priority"]
			}
		s.forEach(function(entry) {
			var i = entry.replace("post_","")
			var v = $('#'+entry).val()
			json += ',"'+i+'":"'+v+'"'
		});
		n.forEach(function(entry) {
			var i = entry.replace("post_","")
			var v = $('#'+entry).val()
			if ( v == "" || v == undefined ) v = 0
			json += ',"'+i+'":'+v+''
		});
		json += ',"user_id":0 }'
		console.log(useType + " json="+json);
		$.ajax({
			type : useType,
			headers: {
				'Csrf-Token':csrf
			} ,
			contentType : "application/json; charset=utf-8",
			data : json,
			url : "/api/jobs",
			success : function(data) {
				loadJobsData();
			},
			error : function(response) {
				alert("error " + response.statusText + ":"+ response.status+" - " +response.responseText)
			}
		});
	}	

	$('#submitJobPostButton').on(
			'click', function(e) {
				$('#id').val(0)
				doPostPut(e,0)
			}
	);
	$('#submitJobPutButton').on(
			'click', function(e) {
				var id = $('#id').val()
				doPostPut(e,id)
			}
	);


	$('#updateJobModal').modal({
		keyboard: true,
		backdrop: "static",
		show:false,

	}) .on('show.bs.modal', function() {

		jobIndex=$(this).data('jobid');
		console.log(jobs[jobIndex]);
		j= jobs[jobIndex];
		$('#id').val(  j.id);
		$('#description').val(  j.description);
		$('#actual_effort').val(  j.actual_effort);
		$('#required_effort').val(  j.required_effort);
		$('#ideal_daily_effort').val(  j.ideal_daily_effort);
		$('#start_date').val(  j.start_date);
		$('#end_date').val(  j.end_date);
		$('#priority').val(  j.priority);

	});

}