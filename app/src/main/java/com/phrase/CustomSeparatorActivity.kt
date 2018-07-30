package com.phrase

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_custom_separator.*

class CustomSeparatorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_separator)

        val customSeparatorString = getString(R.string.text_phrase_custom)

        tv_description.text = "自定义分割符"
        tv_original.text = customSeparatorString

        val separator = "()"

        // 设置字体和颜色
        val colorAndSize = StylePhrase(customSeparatorString)
                .setFirstSeparator(separator)
                .setInnerFirstColor(Color.BLUE)
                .setInnerFirstSize(20)
        tv_content.text = colorAndSize.format()

        // 设置粗斜体
        val boldPhrase = StylePhrase(customSeparatorString)
        boldPhrase.firstBuilder.separator = separator
        boldPhrase.firstBuilder.addParcelableSpan(StyleSpan(Typeface.BOLD_ITALIC))
        tv_content_bold_italic.text = boldPhrase.format()

        // 设置删除线
        val strikeThroughPhrase = StylePhrase(customSeparatorString)
        strikeThroughPhrase.firstBuilder.separator = separator
        strikeThroughPhrase.firstBuilder.addParcelableSpan(StrikethroughSpan())
        tv_content_strike_through.text = strikeThroughPhrase.format()

        // 设置下划线
        val underlinePhrase = StylePhrase(customSeparatorString)
        underlinePhrase.firstBuilder.separator = separator
        underlinePhrase.firstBuilder.addParcelableSpan(UnderlineSpan())
        tv_content_underline.text = underlinePhrase.format()

        val multiClickText = getString(R.string.text_phrase_multi_click)
        val multiClickPhrase = StylePhrase(multiClickText)
        multiClickPhrase.firstBuilder.setSize(20).addParcelableSpan(object : ClickableSpan() {
            override fun onClick(widget: View?) {
                showToast("跳转百度")
            }
        })
        multiClickPhrase.secondBuilder.setSize(15).addParcelableSpan(object : ClickableSpan() {
            override fun onClick(widget: View?) {
                showToast("跳转网易")
            }

            override fun updateDrawState(ds: TextPaint?) {
                ds?.color = Color.BLUE
                ds?.isUnderlineText = false
            }
        })
        tv_content_multi_click.text = multiClickPhrase.format()
        tv_content_multi_click.movementMethod = LinkMovementMethod.getInstance()


        tv_separator.text = colorAndSize.firstBuilder.separator
    }

    fun showToast(msg: String) {
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
    }
}
