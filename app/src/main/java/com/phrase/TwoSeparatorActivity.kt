package com.phrase

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import kotlinx.android.synthetic.main.activity_one_separator.*

class TwoSeparatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_separator)

        val twoSeparatorString = getString(R.string.text_phrase_two)

        tv_description.text = "二种分割符"
        tv_original.text = twoSeparatorString

        // 设置字体和颜色
        val colorAndSize = StylePhrase(twoSeparatorString)
                .setInnerFirstColor(Color.BLUE)
                .setInnerFirstSize(20)
                .setInnerSecondColor(Color.RED)
                .setInnerSecondSize(25)
        tv_content.text = colorAndSize.format()

        // 设置粗斜体
        val boldPhrase = StylePhrase(twoSeparatorString)
        boldPhrase.setInnerFirstColor(Color.RED)
        boldPhrase.setInnerSecondColor(Color.BLUE)
        boldPhrase.setInnerSecondSize(13)
        boldPhrase.secondBuilder.addParcelableSpan(StyleSpan(Typeface.BOLD_ITALIC))
        tv_content_bold_italic.text = boldPhrase.format()

        // 设置删除线
        val strikeThroughPhrase = StylePhrase(twoSeparatorString)
        strikeThroughPhrase.firstBuilder.setColor(Color.BLUE)
        strikeThroughPhrase.firstBuilder.addParcelableSpan(StrikethroughSpan())
        strikeThroughPhrase.setInnerSecondSize(25)
        tv_content_strike_through.text = strikeThroughPhrase.format()

        // 设置下划线
        val underlinePhrase = StylePhrase(twoSeparatorString)
        underlinePhrase.secondBuilder.addParcelableSpan(UnderlineSpan())
        tv_content_underline.text = underlinePhrase.format()

        tv_separator.text = "${colorAndSize.firstBuilder.separator} ${colorAndSize.secondBuilder.separator}"
    }
}
