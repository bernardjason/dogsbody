package controllers

import views.html.helper.FieldConstructor
import views.html.formhelp

object FormHelper {
  implicit val custom = FieldConstructor(formhelp.f)
}