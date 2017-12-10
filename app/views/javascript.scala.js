@(user:User)
@import helper._

var chartdata = null ; //new google.visualization.DataTable();;
var chart = null;
var chartoptions = null;

setupJobChart();


var currentDate= new Date()
var jobs = []
function getAdjustedWeek(addToIt) {
	var d = currentDate
	var offset =  addToIt *60*60*24*1000
	d.setTime(d.getTime() + offset)		

	var yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));

	var weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7);

	return ""+d.getFullYear()+"-W"+ weekNo
}

function selectWeek(direction) {
	$('#week').val(getAdjustedWeek(direction*7))
	loadTimeEntryData(currentDate);
}

function firstDayOfWeek (both) {

	var year = both.substring(0, 4)
	var week = parseInt( both.substring(6,9) )

	var d = new Date(year, 0, 1),
	offset = d.getTimezoneOffset();

	d.setDate(d.getDate() + 4 - (d.getDay() || 7));

	d.setTime(d.getTime() + 7 * 24 * 60 * 60 * 1000 
			* (week + (year == d.getFullYear() ? -1 : 0 )));

	d.setTime(d.getTime() 
			+ (d.getTimezoneOffset() - offset) * 60 * 1000);

	d.setDate(d.getDate() - 3);

	return d ;
}

function loadTimeEntryData(forDate) {

	$("#timeentries").empty();
	$("#timeentries").append('<div class="loader"></div>')

	var week = null
	if (  $('#week').val() != "" ) {
		var months = ['January','February','March','April','May','June','July','August','September','October','November','December'];

		var first = forDate
		if ( first  === "undefined" || first == null ) first = firstDayOfWeek( $('#week').val()  )

		currentDate = first
		var show = "Monday "+ first.getDate() +' ' +months[first.getMonth()] +' '+first.getUTCFullYear() 

		week = "week="+encodeURIComponent( first.getTime() )
		$("#displayweek").text(show);
	} else {
		$("#displayweek").text("All");
	}	

	var total=0.0	
	$.getJSON(			
			"api/timeentries", week,
			function(data) {
				$("#timeentries").empty();
				$.each(	data,function(key, j) {
					var head = '<tr>'

						var newone = '<td>'+ j.task+ '</td><td>' + j.effort + '</td>' +
						'<td><button type="button" onClick="deleteEntry('+j.id+')" class="btn btn-danger">-</button></td>'
						var tail = '</tr>'
							total=total+j.effort

							$("#timeentries").append( head + newone +tail ); 
				});

				$('#total').text(total)

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
       legend: { position: 'bottom' },
        lineWidth: 0,
    	connectSteps: false,
    	chartArea:{top:0},
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

      chart = new google.visualization.SteppedAreaChart(document.getElementById('chart_div'));
      /*
      chartdata = google.visualization.arrayToDataTable([
          ['Date','Free' , 'Task1','Task2','Task3'],
          ['Monday',10,10,10,jobs[0].priority],
          ['Tuesday',5,5,8,12],
          ['Wednesday',6,6,6,12],
          ['Thursday',4,4,4,18],
          ['Friday',9,9,9,3]
        ]);
        */
      
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
			if ( j.start_date_seconds < today && j.end_date_seconds > today ) {
				jobsFromToday.push(j)
				console.log("from today "+j);
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
			    if ( j.end_date_seconds > d-day ) {
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
					var xhead = '<tr data-toggle="modal" onClick="selectJob('+accordion+')" data-id="jobrow'+accordion+'" data-target="#updateJobModal" >'
					var head = '<tr>'

						console.log(j);

					var newone = 
						'<td><a href="#updateJobModal" class="btn btn-default btn-block" onClick="selectJob('+accordion+')" data-id="jobrow'+accordion+'">' + j.description + '</a></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'required_effort\',0)" class="form-control input-sm" type="number"  min="0" max="10" step="0.25" value="' + j.required_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'actual_effort\',0)" class="form-control input-sm" type="number"  min="0" max="10" step="0.25" value="' + j.actual_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'ideal_daily_effort\',0)" class="form-control input-sm" type="number"  min="0" max="10" step="0.25" value="' + j.ideal_daily_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'estimate_left_effort\',0)" class="form-control input-sm" type="number"  min="0" max="10" step="0.25" value="' + j.estimate_left_effort + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'start_date\',1)" class="form-control input-sm" type="date"   value="' + j.start_date + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'end_date\',1)" class="form-control input-sm" type="date"   value="' + j.end_date + '"/></td>' +
						'<td><input onchange="updateValue(this,'+accordion+',\'priority\',0)" class="form-control input-sm" type="number" min="0" max="99" value="' + j.priority + '"/></td>' +
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
			// loadJobsData();
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
function addTask() {
	$('#addTaskPost').modal('show');
}

function selectJob(j) {
	$('#updateJobModal').data('jobid',j);
	$('#updateJobModal').modal('show')
}

function loadJavascript(){
	$('#submitTaskPostButton').on(
			'click',
			function(e) {
				var user = $('#user').val()

				var e = $("#task_id");
				var task_option = $("#task_id option:selected").val();
				var task = $("#task_id option:selected").text();
				var effort = $("#effort").val();
				var first = firstDayOfWeek( $('#week').val()  )
				var when = first.getTime() 
				var csrf = $('input[name=csrfToken]').val()
				var json = '{ "id":0 ,"task_id":'+ task_option+' , "when":"'+ when+'" , "task":"'+task+'","effort":'+effort+' }'

				$.ajax({
					type : "POST",
					headers: {
						'Csrf-Token':csrf
					} ,
					contentType : "application/json; charset=utf-8",
					data : json,
					url : "/api/timeentry",
					success : function(data) {
						loadTimeEntryData();
					},
					error : function(response) {
						alert("error " + response.statusText + ":"+ response.status+" - " +response.responseText)
					}
				});
			});


	function doPostPut(e,id) {
		var user = $('#user').val()

		var e = $("#task_id");
		var csrf = $('input[name=csrfToken]').val()

		var s = ["description","start_date","end_date"]
		var n = ["required_effort","actual_effort","ideal_daily_effort","estimate_left_effort","priority"]

		console.log("ID IS "+id)
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
			console.log(json);
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

		console.log('shown baby!');
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

	$('#postTaskButton').hover(	
			function() {
				addTask();
			}
	);
	$('#postTaskButton').click(	
			function() {
				$('#addTaskPost').modal('hide');
			}
	);
	if ( $('#week').length > 0 ) {
		$('#week').val(getAdjustedWeek(0))

		loadTimeEntryData();

		@if( user == null ) {
			$('#postTaskButton').prop('disabled', true);
		} else {
			$('#postTaskButton').prop('disabled', false);
		}
	}
}