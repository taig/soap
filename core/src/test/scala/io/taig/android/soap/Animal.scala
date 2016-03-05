package io.taig.android.soap

sealed trait Animal
case class Dog( name: String ) extends Animal
case class Cat( moody: Boolean ) extends Animal
case class Mouse( age: Int ) extends Animal
sealed trait Bird extends Animal
object Bird {
    case class Eagle( weight: Option[Float], hunts: List[Animal] ) extends Bird
}

sealed trait Enumeration
object Enumeration {
    case object A extends Enumeration
    case object B extends Enumeration
}