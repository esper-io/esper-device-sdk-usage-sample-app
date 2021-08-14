package io.esper.sdksample.enum

import android.view.View.OnClickListener
import android.widget.CompoundButton.OnCheckedChangeListener

sealed class InputType(
    val primaryHint: String? = null,
    val primaryText: String? = null,

    val secondaryHint: String? = null,
    val secondaryText: String? = null,

    val arrayResourceId: Int? = null,

    val switchText: String? = null,
    val switchCheckedChangeListener: OnCheckedChangeListener? = null,

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
    secondaryHint: String? = null,
    secondaryText: String? = null,
    buttonText: String? = null,
    buttonClickListener: OnClickListener? = null
) :
    InputType(
        primaryHint = primaryHint,
        primaryText = primaryText,
        secondaryHint = secondaryHint,
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

class Switch(
    switchText: String? = null,
    switchCheckedChangeListener: OnCheckedChangeListener? = null
) :
    InputType(
        switchText = switchText,
        switchCheckedChangeListener = switchCheckedChangeListener
    )

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