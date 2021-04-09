package swimr

import akka.actor.Cancellable
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}



object PingActor{
	def apply():Behavior[String] = {
		Behaviors.setup(context => {
			new PingActor(context)
		})
	}
}

class PingActor(context:ActorContext[String]) extends AbstractBehavior[String](context){

	var cx:Option[Cancellable] = None

	override def onMessage(msg: String): Behavior[String] = {
		msg match {
			case "start" => {
				println("[PingActor] 'start' received")

				val pongActor:ActorRef[PongActor.Ping] = context.spawn(PongActor(), "pong-actor")
				println("[PingActor] sending 'ping' to PongActor")

				 // val system = akka.actor.ActorSystem("system")
				 // import system.dispatcher
				import scala.concurrent.ExecutionContext.Implicits.global
				// system.scheduler.scheduleOnce(Duration(1000,MILLISECONDS)) {
				// 	pongActor ! PongActor.Ping(context.self)
				// }

				import scala.concurrent.duration.FiniteDuration
				import scala.concurrent.duration.MILLISECONDS
				cx = Some(context.system.scheduler.scheduleWithFixedDelay(
					FiniteDuration(0,MILLISECONDS),
					FiniteDuration(1000,MILLISECONDS))(
						new Runnable(){ def run() = {
							pongActor ! PongActor.Ping(context.self)
						}
					})
				)
				this
			}
			case "stop" => {

				println("[PingActor] 'stop' received")

				cx match {
					case None => {
						println("[PingActor] 'stop' received, but cx is None")
					}
					case Some(cancellable) => {
						println("[PingActor] 'stop' received, canceling the cancellable")
						cancellable.cancel();
					}
				}


				this
			}
			case "confirmed" =>{
				println("[PingActor] 'confirmed' received")
				this

			}
		}
	}

	def sendPing(pongActor: ActorRef[PongActor.Ping]):Unit = {
		println("[PingActor] sending ping")
		pongActor ! PongActor.Ping(context.self)

	}
}

object PongActor{
	final case class Ping(sender:ActorRef[String])

	def apply():Behavior[PongActor.Ping] ={
		Behaviors.setup(context => new PongActor(context))
	}
}

class PongActor(context:ActorContext[PongActor.Ping]) extends AbstractBehavior[PongActor.Ping](context){
	override def onMessage(msg:PongActor.Ping):Behavior[PongActor.Ping]={

		println("[PongActor] got a ping, sending 'confirmed'")

		msg.sender ! "confirmed"

		Behaviors.same

	}
}


object GuardianActor {

	trait Command
	final case class SayHello(message_text:String) extends Command
	final case class ShutItAllDown() extends Command

	// the type of message to be handled is declared to be of type SayHello
	// which means the message argument has the same type
	def apply(): Behavior[Command] = {
		println("[MyActorMain::apply] MyActorMain alive")

		// bootstrap the actor system
		Behaviors.setup({ context =>
			println("[MyActorMain.apply]")
			val pingActor = context.spawn(PingActor(), "pinger")
			// pattern match not needed here because message will (even) implicitly
			// get the SayHello type ([T])
			Behaviors.receiveMessage(message => {

				message match {
					case m:SayHello =>
						println("[GuardianActor] sending 'start' to PingActor")
						// "the next behavior is the same as the current one"
						pingActor ! "start"

					case m:ShutItAllDown => {
						pingActor ! "stop" // already
					}
				}


				Behaviors.same
			})
		})
	}
}

object Main extends App {
//	val ages = Seq(42, 75, 29, 64)
//	println(s"The oldest person is ${ages.max}")

	val myActorMain:ActorSystem[GuardianActor.Command] = ActorSystem(GuardianActor(),"MyActorTest")

	myActorMain ! GuardianActor.SayHello("[Main] this is a message from Main to MyActorMain")

	Thread.sleep(5000)

	myActorMain ! GuardianActor.ShutItAllDown()


}
