package com.naposystems.pepito.utility

import com.google.android.material.textfield.TextInputLayout
import com.naposystems.pepito.R
import java.util.regex.Pattern

class FieldsValidator {

    companion object {
        private val specialCharactersPattern: Pattern =
            Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]")

        private val containNumberPattern: Pattern =
            Pattern.compile(".*[0-9].*")

        fun isNicknameValid(textInputLayout: TextInputLayout): Boolean {
            val nickname = textInputLayout.editText!!.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            if (nickname.isEmpty()) {
                textInputLayout.error = context.getString(R.string.nickname_required)
                return false
            }

            if (nickname.length < 5) {
                textInputLayout.error = context.getString(R.string.at_least_five_characters)
                return false
            }

            if (textContainWitheSpaces(nickname)) {
                textInputLayout.error =
                    context.getString(R.string.nickname_cant_contain_white_spaces)
                return false
            }

            if (textContainSpecialCharacters(nickname)) {
                textInputLayout.error =
                    context.getString(R.string.cant_contain_special_characters)
                return false
            }

            if (!textContainNumber(nickname)) {
                textInputLayout.error = context.getString(R.string.at_least_one_number)
                return false
            }

            return true
        }

        fun isDisplayNameValid(textInputLayout: TextInputLayout): Boolean {
            val displayName = textInputLayout.editText!!.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            if (displayName.isNotEmpty()) {
                if (displayName.length < 5) {
                    textInputLayout.error = context.getString(R.string.at_least_five_characters)
                    return false
                }

                if (textContainWitheSpaces(displayName) && textContainSpecialCharacters(displayName)) {
                    textInputLayout.error =
                        context.getString(R.string.cant_contain_special_characters)
                    return false
                }
            }

            return true
        }

        fun isAccessPinValid(textInputLayout: TextInputLayout): Boolean {
            val acessPin = textInputLayout.editText!!.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            if (acessPin.isEmpty()) {
                textInputLayout.error = context.getString(R.string.access_pin_required)
                return false
            }

            if (acessPin.length < 4) {
                textInputLayout.error = context.getString(R.string.access_pin_length)
                return false
            }

            if (textContainSpecialCharacters(acessPin)) {
                textInputLayout.error =
                    context.getString(R.string.cant_contain_special_characters)
                return false
            }
            return true
        }

        fun isConfirmAccessPinValid(textInputLayout: TextInputLayout, accessPin: String): Boolean {
            val confirmAccessPin = textInputLayout.editText!!.text.toString()
            val context = textInputLayout.context

            textInputLayout.error = null

            if (confirmAccessPin.isEmpty()) {
                textInputLayout.error = context.getString(R.string.confirm_pin_access_required)
                return false
            }

            if (confirmAccessPin.length < 4) {
                textInputLayout.error = context.getString(R.string.access_pin_length)
                return false
            }

            if (textContainSpecialCharacters(confirmAccessPin)) {
                textInputLayout.error =
                    context.getString(R.string.cant_contain_special_characters)
                return false
            }

            if (accessPin != confirmAccessPin) {
                textInputLayout.error = context.getString(R.string.access_pin_not_match)
                return false
            }
            return true
        }

        private fun textContainWitheSpaces(text: String) = text.contains(' ')

        private fun textContainSpecialCharacters(text: String) =
            specialCharactersPattern.matcher(text).find()

        private fun textContainNumber(text: String) =
            containNumberPattern.matcher(text).find()
    }
}