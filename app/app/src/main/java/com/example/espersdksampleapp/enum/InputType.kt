package com.example.espersdksampleapp.enum

sealed class InputType(
    val primaryHint: String? = null,
    val secondaryHint: String? = null,
    val arrayResourceId: Int? = null,
    val switchText: String? = null,
    val buttonText: String? = null
)

class OneTextField(hint: String, buttonText: String? = null) :
    InputType(primaryHint = hint, buttonText = buttonText)

class TwoTextField(primaryHint: String, secondaryHint2: String) :
    InputType(primaryHint = primaryHint, secondaryHint = secondaryHint2)

class Spinner(arrayResourceId: Int? = null, buttonText: String? = null) :
    InputType(arrayResourceId = arrayResourceId, buttonText = buttonText)

class Switch(switchText: String? = null) : InputType(switchText = switchText)

class OneTextFieldOneSpinner(buttonText: String? = null) : InputType(buttonText = buttonText)