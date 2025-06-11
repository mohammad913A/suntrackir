package com.example.suntrackir

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.*
/*
برای انتخاب تاریخ قمری نیز می‌توان از DatePickerDialog به صورت نمونه استفاده کرد.
این پیاده‌سازی نمونه تاریخ انتخاب شده را به صورت org.threeten.bp.LocalDate برمی‌گرداند.
*/
class HijriDatePickerDialog(
    private val initialDate: LocalDate,
    private val listener: (LocalDate) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val threeTenInstant = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val calendar = Calendar.getInstance().apply {
            time = Date(threeTenInstant.toEpochMilli())
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(requireContext(), { _, y, m, d ->
            // در پیاده‌سازی واقعی تبدیل مناسب انجام شود؛ در اینجا همان تبدیل میلادی استفاده می‌شود.
            val selectedDate = LocalDate.of(y, m + 1, d)
            listener(selectedDate)
        }, year, month, day)
    }
}