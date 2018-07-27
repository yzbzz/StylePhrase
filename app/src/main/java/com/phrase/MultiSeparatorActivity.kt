package com.phrase

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import kotlinx.android.synthetic.main.activity_one_separator.*

class MultiSeparatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_separator)

        val multiSeparatorString = getString(R.string.text_phrase_three)

        tv_description.text = "多种分割符"
        tv_original.text = multiSeparatorString

        // 设置字体和颜色
        val colorAndSize = StylePhrase(multiSeparatorString)
                .setInnerFirstColor(Color.BLUE)
                .setInnerFirstSize(20)
                .setInnerSecondColor(Color.RED)
                .setInnerSecondSize(25)

        val builder = StylePhrase.Builder()
        builder.separator = "()"
        builder.setColor(Color.GREEN)
        builder.setSize(18)
        colorAndSize.addBuilder(builder)
        tv_content.text = colorAndSize.format()

        // 设置粗斜体
        val boldPhrase = StylePhrase(multiSeparatorString)
        boldPhrase.setInnerFirstColor(Color.RED)
        boldPhrase.setInnerSecondColor(Color.BLUE)
        boldPhrase.setInnerSecondSize(13)
        val boldBuilder = StylePhrase.Builder()
        boldBuilder.separator = "()"
        boldBuilder.addParcelableSpan(StyleSpan(Typeface.BOLD_ITALIC))
        boldPhrase.addBuilder(boldBuilder)
        tv_content_bold_italic.text = boldPhrase.format()

        // 设置删除线
        val strikeThroughPhrase = StylePhrase(multiSeparatorString)
        strikeThroughPhrase.firstBuilder.setColor(Color.BLUE)
        strikeThroughPhrase.setInnerSecondSize(25)
        val strikeThroughBuilder = StylePhrase.Builder()
        strikeThroughBuilder.separator = "()"
        strikeThroughBuilder.addParcelableSpan(StrikethroughSpan())
        strikeThroughPhrase.addBuilder(strikeThroughBuilder)
        tv_content_strike_through.text = strikeThroughPhrase.format()

        // 设置下划线
        val underlinePhrase = StylePhrase(multiSeparatorString)
        underlinePhrase.setInnerSecondColor(Color.GREEN)
        val underlineBuilder = StylePhrase.Builder()
        underlineBuilder.separator = "()"
        underlineBuilder.addParcelableSpan(UnderlineSpan())
        underlinePhrase.addBuilder(underlineBuilder)
        tv_content_underline.text = underlinePhrase.format()

        var builders = colorAndSize.builders
        val sb = StringBuilder()
        for (builder in builders) {
            sb.append(builder.separator)
            sb.append(" ")
        }
        tv_separator.text = sb.toString()
    }
}
