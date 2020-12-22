package com.example.calculator

enum class ButtonAction(
    val text: String
) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    PERCENT("%"),
    DOT("."),
    CALCULATE("="),
    PLUS("+"),
    MINUS("-"),
    DEL("del"),
    MULTIPLY("*"),
    DIVIDE("/"),
    CLEAR("C"),
    M_R("mr"),
    M_PLUS("m+"),
    M_MINUS("m-"),
    M_CLEAR("mc");

    companion object {
        fun isNumberAction(
            action: ButtonAction
        ) = action in listOf(ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE)

        fun isOperatorAction(
            action: ButtonAction
        ) = action in listOf(PLUS, MINUS, MULTIPLY, DIVIDE)
    }
}
