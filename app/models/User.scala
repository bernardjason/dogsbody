package models

case class User(id:Long,user:String, password:String,nickname:String,role:Option[String]) 


object User extends  ((Long,String ,String,String,Option[String]) => User) {

  def apply(user:String,password:String,nickname:String,role:Option[String]):User = User(0,user,password,nickname,role)
  def unpick(u: User): Option[ (Long,String,String,String,Option[String])]  = Some(u.id,u.user,u.password,u.nickname,u.role)
  
  def apply(user:String,password:String,nickname:String):User = User(0,user,password,nickname,None)
  def unpicknorole(u: User): Option[ (String,String,String)]  = Some(u.user,u.password,u.nickname)
}
