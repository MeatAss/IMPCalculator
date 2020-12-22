package com.example.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.ButtonAction.CALCULATE
import com.example.calculator.ButtonAction.CLEAR
import com.example.calculator.ButtonAction.Companion.isNumberAction
import com.example.calculator.ButtonAction.Companion.isOperatorAction
import com.example.calculator.ButtonAction.DEL
import com.example.calculator.ButtonAction.DIVIDE
import com.example.calculator.ButtonAction.DOT
import com.example.calculator.ButtonAction.MINUS
import com.example.calculator.ButtonAction.MULTIPLY
import com.example.calculator.ButtonAction.M_CLEAR
import com.example.calculator.ButtonAction.M_MINUS
import com.example.calculator.ButtonAction.M_PLUS
import com.example.calculator.ButtonAction.M_R
import com.example.calculator.ButtonAction.PERCENT
import com.example.calculator.ButtonAction.PLUS
import com.example.calculator.ButtonAction.valueOf
import com.example.calculator.ButtonAction.values

class MainActivity : AppCompatActivity() {
    private lateinit var textViewMain: TextView
    private lateinit var textViewMode: TextView
    private lateinit var textViewPreCalculate: TextView

    private var leftSideNumber: String = EMPTY
    private var rightSideNumber: String = EMPTY
    private var operation: ButtonAction? = null
    private var bufferValue: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewMain = findViewById(R.id.textViewMain)
        textViewMode = findViewById(R.id.textViewMode)
        textViewPreCalculate = findViewById(R.id.textViewPreCalculate)
    }

    fun onButtonClick(view: View) {
        getButtonActionByText(view)
            ?.let(::proceedButton)
    }

    private fun getButtonActionByText(view: View) =
        (view as? Button)
            ?.text
            ?.toString()
            ?.let { text ->
                values().firstOrNull {
                    it.text == text
                }
            }

    private fun proceedButton(
        action: ButtonAction
    ) {
        when {
            isNumberAction(action) -> proceedNumber(action)
            isOperatorAction(action) -> proceedOperator(action)
            action == CLEAR -> proceedClear()
            action == CALCULATE -> proceedCalculate()
            action == DOT -> proceedDot()
            action == DEL -> proceedDel()
            action == M_PLUS -> proceedMPlus()
            action == M_MINUS -> proceedMMinus()
            action == M_CLEAR -> proceedMClear()
            action == M_R -> proceedMR()
            action == PERCENT -> proceedPercent()
        }

        updatePreCalculate()
    }

    private fun updatePreCalculate() {
        textViewPreCalculate.text = calculateValue()?.let {
            if (it != CALCULATE_EXCEPTION_KEY) {
                it.takeIf { operation != null && rightSideNumber.isNotEmpty() }
                    ?.toString()
                    ?: EMPTY
            } else {
                CALCULATE_EXCEPTION_KEY
            }
        }
    }

    private fun proceedPercent() {
        if (operation != null && rightSideNumber.toDoubleOrNull() != null) {
            rightSideNumber += "%"
            putText("%")
        }
    }

    private fun proceedDel() {
        when {
            rightSideNumber.isNotEmpty() -> dropLastText { rightSideNumber = rightSideNumber.dropLast(1) }
            operation != null -> dropLastText { operation = null }
            leftSideNumber.isNotEmpty() -> dropLastText { leftSideNumber = leftSideNumber.dropLast(1) }
        }
    }

    private fun proceedDot() {
        if (operation == null) {
            if (!leftSideNumber.contains(".")) {
                leftSideNumber = "$leftSideNumber."
                putText(",")
            }
        } else {
            if (rightSideNumber.isNotEmpty() && !rightSideNumber.contains(".")) {
                rightSideNumber = "$rightSideNumber."
                putText(",")
            }
        }
    }

    private fun proceedMR() {
        if (bufferValue == null) return

        checkNotNull(bufferValue)
            .toString()
            .also { value ->
                if (leftSideNumber.isNotEmpty() && operation != null && rightSideNumber.isEmpty()) {
                    rightSideNumber = value
                    putText(value)
                } else {
                    proceedClear()
                    leftSideNumber = value
                    putText(value)
                }
            }
    }

    private fun proceedMClear() {
        bufferValue = null
        textViewMode.text = EMPTY
    }

    private fun proceedMPlus() {
        calculateValue()
            ?.takeIf { it != CALCULATE_EXCEPTION_KEY }
            ?.toDouble()
            ?.let { value ->
                if (bufferValue == null) {
                    textViewMode.text = "M"
                }
                bufferValue = bufferValue?.let { it + value } ?: value
            }
    }

    private fun proceedMMinus() {
        calculateValue()
            ?.takeIf { it != CALCULATE_EXCEPTION_KEY }
            ?.toDouble()
            ?.let { value ->
                if (bufferValue == null) {
                    textViewMode.text = "M"
                }
                bufferValue = bufferValue?.let { it - value } ?: value
            }
    }

    private fun proceedCalculate() {
        if (leftSideNumber.isEmpty() && operation == null && rightSideNumber.isEmpty()) return

        if (leftSideNumber.isNotEmpty() && rightSideNumber.isEmpty()) {
            textViewMain.text = leftSideNumber
        }

        if (leftSideNumber.isNotEmpty() && operation != null && rightSideNumber.isNotEmpty()) {
            calculate(leftSideNumber, rightSideNumber)?.let {
                proceedClear()
                textViewMain.text = it
                leftSideNumber = it
            }
        }
    }

    private fun calculateValue(): String? {
        if (leftSideNumber.isNotEmpty() && operation != null && rightSideNumber.isNotEmpty()) {
            return calculate(leftSideNumber, rightSideNumber)
        }

        if (leftSideNumber.isNotEmpty() && rightSideNumber.isEmpty()) {
            return leftSideNumber
        }

        return null
    }

    private fun calculate(
        leftSideNumber: String,
        rightSideNumber: String
    ) = try {
        when (operation) {
            PLUS -> calculatePlus(leftSideNumber, rightSideNumber)
            MINUS -> calculateMinus(leftSideNumber, rightSideNumber)
            MULTIPLY -> calculateMultiply(leftSideNumber, rightSideNumber)
            DIVIDE -> calculateDivide(leftSideNumber, rightSideNumber)
            else -> null
        }?.toString()?.replace(Regex("\\.0+$"), EMPTY)
    } catch (ex: Exception) {
        CALCULATE_EXCEPTION_KEY
    }

    private fun String.toDoubleSave() =
        replace("%", EMPTY).toDouble()

    private fun calculatePlus(
        leftSideNumber: String,
        rightSideNumber: String
    ) = if (rightSideNumber.lastOrNull() != '%') {
        leftSideNumber.toDoubleSave() + rightSideNumber.toDoubleSave()
    } else {
        leftSideNumber.toDoubleSave() * (1 + rightSideNumber.toDoubleSave() / 100)
    }

    private fun calculateMinus(
        leftSideNumber: String,
        rightSideNumber: String
    ) = if (rightSideNumber.lastOrNull() != '%') {
        leftSideNumber.toDoubleSave() - rightSideNumber.toDoubleSave()
    } else {
        leftSideNumber.toDoubleSave() * (1 - rightSideNumber.toDoubleSave() / 100)
    }

    private fun calculateMultiply(
        leftSideNumber: String,
        rightSideNumber: String
    ) = if (rightSideNumber.lastOrNull() != '%') {
        leftSideNumber.toDoubleSave() * rightSideNumber.toDoubleSave()
    } else {
        leftSideNumber.toDoubleSave() * (rightSideNumber.toDoubleSave() / 100)
    }

    private fun calculateDivide(
        leftSideNumber: String,
        rightSideNumber: String
    ) = if (rightSideNumber.lastOrNull() != '%') {
        leftSideNumber.toDoubleSave() / rightSideNumber.toDoubleSave()
    } else {
        leftSideNumber.toDoubleSave() / (rightSideNumber.toDoubleSave() / 100)
    }

    private fun proceedOperator(action: ButtonAction) {
        action.text.also {
            if (leftSideNumber.isEmpty()) return

            if (operation == null) {
                operation = action
                putText(it)
            }

            if (operation != action && getLastOperatorInText() == true) {
                dropLastText {
                    operation = action
                    putText(it)
                }
            }
        }
    }

    private fun getLastOperatorInText() = textViewMain.text
        .toString()
        .lastOrNull()
        ?.toString()
        ?.let { if (values().contains(it)) valueOf(it) else null }
        ?.let(::isOperatorAction)

    private fun proceedNumber(action: ButtonAction) {
        action.text.also { number ->
            if (operation == null) {
                leftSideNumber = "$leftSideNumber$number"
                putText(number)
            } else {
                if (rightSideNumber.lastOrNull() != '%') {
                    rightSideNumber = "$rightSideNumber$number"
                    putText(number)
                }
            }
        }
    }

    private fun proceedClear() {
        leftSideNumber = EMPTY
        rightSideNumber = EMPTY
        textViewMain.text = EMPTY
        operation = null
    }

    private fun putText(value: String) {
        textViewMain.text = textViewMain.text.toString() + value
    }

    private fun dropLastText(
        afterDrop: () -> Unit
    ) {
        textViewMain.text = textViewMain.text.toString().dropLast(1)
        afterDrop()
    }

    companion object {
        private const val EMPTY = ""
        private const val CALCULATE_EXCEPTION_KEY = "Ошибка!"
    }
}
