package com.naposystems.pepito.utility

import com.google.android.material.textfield.TextInputLayout
import com.naposystems.pepito.R
import java.util.regex.Pattern

class FieldsValidator {

    companion object {
        private val specialCharactersPattern: Pattern =
            Pattern.compile("[$&+,:;=\\\\?@#|/'<>^*()%!-]")

        private val containNumberPattern: Pattern =
            Pattern.compile(".*[0-9].*")

        private val containAtLeastLetterPattern: Pattern =
            Pattern.compile(".*[a-zA-Z].*")

        fun isNicknameValid(textInputLayout: TextInputLayout): Boolean {
            val nickname = textInputLayout.editText?.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            when {
                (nickname.isEmpty()) -> {
                    textInputLayout.error = context.getString(R.string.text_nickname_required)
                    return false
                }
                (textContainWitheSpaces(nickname)) -> {
                    textInputLayout.error =
                        context.getString(R.string.text_nickname_must_not_contain_space)
                    return false
                }
                (textContainSpecialCharacters(nickname)) -> {
                    textInputLayout.error =
                        context.getString(R.string.text_cant_contain_special_characters)
                    return false
                }
                (!textContainLetter(nickname.substring(0, 1)))->{
                    textInputLayout.error = context.getString(R.string.text_only_start_letter)
                    return false
                }
                (nickname.length < 4) -> {
                    textInputLayout.error =
                        context.getString(R.string.text_nickname_not_contain_enough_char_and_number)
                    return false
                }
                (nickname.length > 3) -> {
                    if (!textContainLetter(nickname)) {
                        textInputLayout.error =
                            context.getString(R.string.text_nickname_contain_at_least_one_letter)
                        return false
                    }

                    if (!textContainNumber(nickname)) {
                        textInputLayout.error =
                            context.getString(R.string.text_nickname_contain_at_least_one_number)
                        return false
                    }

                    if (!textValidLastNumber(nickname)) {
                        textInputLayout.error = context.getString(R.string.text_nickname_unavailable)
                        return false
                    }
                }
            }

            return true
        }

        fun isDisplayNameValid(textInputLayout: TextInputLayout): Boolean {
            val displayName = textInputLayout.editText?.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            if (displayName.isNotEmpty()) {
                if (displayName.length < 2) {
                    textInputLayout.error =
                        context.getString(R.string.text_name_not_contain_enough_char)
                    return false
                }

                if (textContainWitheSpaces(displayName) && textContainSpecialCharacters(displayName)) {
                    textInputLayout.error =
                        context.getString(R.string.text_cant_contain_special_characters)
                    return false
                }
            }

            return true
        }

        fun isAccessPinValid(textInputLayout: TextInputLayout): Boolean {
            val accessPin = textInputLayout.editText?.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            if (accessPin.isEmpty()) {
                textInputLayout.error = context.getString(R.string.text_access_pin_required)
                return false
            }

            if (accessPin.length < 4) {
                textInputLayout.error = context.getString(R.string.text_access_pin_length)
                return false
            }

            if (textContainSpecialCharacters(accessPin)) {
                textInputLayout.error =
                    context.getString(R.string.text_cant_contain_special_characters)
                return false
            }
            return true
        }

        fun isConfirmAccessPinValid(
            textInputLayout: TextInputLayout,
            textInputLayoutConfirm: TextInputLayout
        ): Boolean {
            val accessPin = textInputLayout.editText!!.text.toString()
            val confirmAccessPin = textInputLayoutConfirm.editText!!.text.toString()
            val context = textInputLayoutConfirm.context

            textInputLayout.error = null
            textInputLayoutConfirm.error = null

            if (confirmAccessPin.isEmpty()) {
                textInputLayoutConfirm.error =
                    context.getString(R.string.text_confirm_pin_access_required)
                return false
            }

            if (confirmAccessPin.length < 4) {
                textInputLayoutConfirm.error = context.getString(R.string.text_access_pin_length)
                return false
            }

            if (textContainSpecialCharacters(confirmAccessPin)) {
                textInputLayoutConfirm.error =
                    context.getString(R.string.text_cant_contain_special_characters)
                return false
            }

            if (accessPin != confirmAccessPin) {
                textInputLayout.error = context.getString(R.string.text_access_pin_not_match)
                textInputLayoutConfirm.error = context.getString(R.string.text_access_pin_not_match)
                return false
            }
            return true
        }

        private fun textContainWitheSpaces(text: String) = text.contains(' ')

        private fun textContainSpecialCharacters(text: String) =
            specialCharactersPattern.matcher(text).find()

        private fun textContainNumber(text: String) =
            containNumberPattern.matcher(text).find()

        private fun textContainLetter(text: String) =
            containAtLeastLetterPattern.matcher(text).find()

        private fun textValidLastNumber(text: String): Boolean {
            val lastCharacter: String = text.substring(text.length - 1)
            return if (lastCharacter == "1") {
                val str: String = text.substring(text.length - 2)
                val beforeTheLastOne = str.substring(0, str.length - 1)
                textContainNumber(beforeTheLastOne)
            } else true
        }
    }
}