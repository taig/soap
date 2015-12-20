package test.io.taig.android

sealed trait Animal

case class Dog( name: String ) extends Animal
case class Cat( moody: Boolean ) extends Animal
case class Mouse( age: Int ) extends Animal

sealed trait Bird extends Animal

object Bird {
    case class Eagle( weight: Option[Float], hunts: List[Animal] ) extends Bird
}

sealed trait Enum
object Enum {
    case object A extends Enum
    case object B extends Enum
}

sealed trait Vehicle
case class Car( seats: Int ) extends Vehicle
object Military {
    case class Car( weight: Float ) extends Vehicle
}