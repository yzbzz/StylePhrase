package com.phrase

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import kotlinx.android.synthetic.main.activity_one_separator.*

class OneSeparatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_separator)

        val oneSeparatorString = getString(R.string.text_phrase_one)

        tv_description.text = "一种分割符"
        tv_original.text = oneSeparatorString

        // 设置字体和颜色
        val colorAndSize = StylePhrase(oneSeparatorString)
                .setInnerFirstColor(Color.BLUE)
                .setInnerFirstSize(20)
        tv_content.text = colorAndSize.format()

        // 设置粗斜体
        val boldPhrase = StylePhrase(oneSeparatorString)
        boldPhrase.firstBuilder.addParcelableSpan(StyleSpan(Typeface.BOLD_ITALIC))
        tv_content_bold_italic.text = boldPhrase.format()

        // 设置删除线
        val strikeThroughPhrase = StylePhrase(oneSeparatorString)
        strikeThroughPhrase.firstBuilder.addParcelableSpan(StrikethroughSpan())
        tv_content_strike_through.text = strikeThroughPhrase.format()

        // 设置下划线
        val underlinePhrase = StylePhrase(oneSeparatorString)
        underlinePhrase.firstBuilder.addParcelableSpan(UnderlineSpan())
        tv_content_underline.text = underlinePhrase.format()

        tv_separator.text = colorAndSize.firstBuilder.separator
    }
}
