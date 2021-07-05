package com.example.espersdksampleapp.enum

import android.view.View.OnClickListener

sealed class InputType(
    val primaryHint: String? = null,
    val primaryText: String? = null,

    val secondaryHint: String? = null,
    val secondaryText: String? = null,

    val arrayResourceId: Int? = null,

    val switchText: String? = null,

    val buttonText: String? = null,
    val buttonClickListener: OnClickListener? = null
)

class OneTextField(
    hint: String? = null,
    text: String? = null,
    buttonText: String? = null,
    buttonClickListener: OnClickListener? = null
) :
    InputType(
        primaryHint = hint,
        primaryText = text,
        buttonText = buttonText,
        buttonClickListener = buttonClickListener
    )

class TwoTextField(
    primaryHint: String? = null,
    primaryText: String? = null,
    secondaryHint2: String? = null,
    secondaryText: String? = null,
    buttonText: String? = null,
    buttonClickListener: OnClickListener? = null
) :
    InputType(
        primaryHint = primaryHint,
        primaryText = primaryText,
        secondaryHint = secondaryHint2,
        secondaryText = secondaryText,
        buttonText = buttonText,
        buttonClickListener = buttonClickListener
    )

class Spinner(
    arrayResourceId: Int? = null,
    buttonText: String? = null,
    buttonClickListener: OnClickListener? = null
) :
    InputType(
        arrayResourceId = arrayResourceId,
        buttonText = buttonText,
        buttonClickListener = buttonClickListener
    )

class Switch(switchText: String? = null) : InputType(switchText = switchText)

class OneTextFieldOneSpinner(
    hint: String? = null,
    text: String? = null,
    arrayResourceId: Int? = null,
    buttonText: String? = null,
    buttonClickListener: OnClickListener? = null
) :
    InputType(
        primaryHint = hint,
        primaryText = text,
        arrayResourceId = arrayResourceId,
        buttonText = buttonText,
        buttonClickListener = buttonClickListener
    )