// 1. Primero crear paquete: (a) Clic derecho sobre carpeta scala new/package y nombrarlo part1_recap.
// 2. Sobre la carpeta part1_recap clic derecho new/Scalaclass y colocar nombre y seleccionar object

package part1_recap

import scala.concurrent.Future
import scala.util.Success
import scala.util.Failure


object ScalaRecap extends App {
  val aCondition: Boolean = false

  def myFunction(x: Int) = {
    //code
    if (x > 4) 42 else 65
  }

  //instructions vs expressions
  //types + type inference

  // 00 features of Scala
  class Animal

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  object Carnivore

  //generic
  abstract class Mylist[+A]

  //method notations
  1 + 2 //infix notations
  1.+(2)

  //FP
  val anIncrementer: Int => Int = (x: Int) => x + 1
  anIncrementer(1)

  List(1, 2, 3).map(anIncrementer)

  //High order functions (HOF): flatMap, filter
  //for-comprehensions

  //Monads: Option, Try

  //Pattern matching
  val unknown: Any = 2
  val order = unknown match {
    case 1 => "first"
    case 2 => "second"
    case _ => "unknown"
  }

  try {
    //code that can throw an exception
    throw new RuntimeException
  } catch {
    case e: Exception => println("I caught one!")
  }

  /**
   * Scala advanced
   */
  // multihreading

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    // long computation here
    //executed on SOME other thread
    42
  }

  //map, flatMap, filter + other niceties e.g. recover/recoverWith
  future.onComplete {
    case Success(value) =>
      println(s"I found the meaning of the life: $value")
    case Failure(exception) =>
      println(s"I found $exception while searching for the meaning of the life")
  } // on SOME thread

  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case _ => 999
  }
  // based on pattern matching

  // type aliases
  val AkkaReceive: PartialFunction[Int, String] = {
    case 1 => "Helo"
    case _ => "Confused"
  }

  //Implicits
  implicit  val timeout = 3000
  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()
  setTimeout(()=> println("timeout"))(timeout)// other arg list injected by the copiler

  //Conversions
  //1) implicit methods
  case class Person(name: String){
    def greet:String = s"Hi, my name is $name"
  }

  implicit def fromStringToPerson(name:String) = Person (name)
  "Peter".greet
  //fromStringToPerson("Peter").greet

  // 2) implicit classes
  implicit class Dog(name:String){
    def bark = println("Bark!")
  }
  "Lassie".bark
  //new Dog("Lassie").bark

  // implicit organizations
  //local scope
  implicit  val numberOrdering: Ordering[Int] = Ordering.fromLessThan(_>_)
  List(1,2,3,4).sorted // numberOrdering => (4,3,2,1)

  //imported scope

  //companion objects of the types involved in the call
  object Person{
    implicit  val personOrdering: Ordering[Person] = {
      Ordering.fromLessThan((a, b) => a.name.compareTo(b.name)<0)
    }
  }
  List(Person("Bob"), Person("Alice")).sorted //(Person.personOrdering)
  //=>List(Person("Alice"),Person("Bob"))
}
