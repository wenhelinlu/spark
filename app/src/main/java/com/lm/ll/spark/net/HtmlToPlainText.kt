package com.lm.ll.spark.net

import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor


/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to convert HTML input to lightly-formatted
 * plain-text. That is divergent from the general goal of jsoup's .text() methods, which is to get clean data from a
 * scrape.
 *
 *
 * Note that this is a fairly simplistic formatter -- for real world use you'll want to embrace and extend.
 *
 *
 *
 * To invoke from the command line, assuming you've downloaded the jsoup jar to your current directory:
 *
 * `java -cp jsoup.jar org.jsoup.examples.HtmlToPlainText url [selector]`
 * where *url* is the URL to fetch, and *selector* is an optional CSS selector.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class HtmlToPlainText {
    /**
     * Format an Element to plain-text
     * @param element the root element to format
     * @return formatted text
     */
    fun getPlainText(element: Element?): String {
        val formatter = FormattingVisitor()
        NodeTraversor.traverse(formatter, element) // walk the DOM, and call .head() and .tail() for each node
        return formatter.toString()
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor : NodeVisitor {
        private var width = 0
        private val accum = StringBuilder() // holds the accumulated text

        // hit when the node is first seen
        override fun head(node: Node, depth: Int) {
            val name: String = node.nodeName()
            when {
                node is TextNode -> append((node as TextNode).text()) // TextNodes carry all user-readable text in the DOM.
                name == "li" -> append("\n * ")
                name == "dt" -> append("  ")
                StringUtil.`in`(name, "p", "h1", "h2", "h3", "h4", "h5", "tr") -> append("\n")
            }
        }

        // hit when all of the node's children (if any) have been visited
        override fun tail(node: Node, depth: Int) {
            val name: String = node.nodeName()
            /**
             * @date 2020年7月2日 13点58分
             * @author luhui
             * @desc 原始网页中带有的换行符(br)是为了在网页中显示换行效果，在此处将网页中的文本格式化在app中
             * 显示时，不需要通过br来换行。所以在下面的判断中将br条件去掉，避免app显示文本时出现
             * 不需要的换行效果
             *
             */
//            if (StringUtil.`in`(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) append("\n") else if (name == "a") append(java.lang.String.format(" <%s>", node.absUrl("href")))
            if (StringUtil.`in`(name, "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5")) append("\n") else if (name == "a") append(java.lang.String.format(" <%s>", node.absUrl("href")))
        }

        // appends text to the string builder with a simple word wrap method
        private fun append(text: String) {
            accum.append(text)
            /**
             * @date 2020年7月2日 14点01分
             * @author luhui
             * @desc 将html转换为文本时，不需要考虑换行显示（app中会自动换行），所以将下面的代码注释掉
             *
             */
//            if (text.startsWith("\n")) width = 0 // reset counter if starts with a newline. only from formats above, not in natural text
//            if (text == " " &&
//                    (accum.isEmpty() || StringUtil.`in`(accum.substring(accum.length - 1), " ", "\n"))) return  // don't accumulate long runs of empty spaces
//            if (text.length + width > maxWidth) { // won't fit, needs to wrap
//                val words = text.split("\\s+".toRegex()).toTypedArray()
//                for (i in words.indices) {
//                    var word = words[i]
//                    val last = i == words.size - 1
//                    if (!last) // insert a space if not the last word
//                        word = "$word "
//                    if (word.length + width > maxWidth) { // wrap and reset counter
//                        accum.append("\n").append(word)
//                        width = word.length
//                    } else {
//                        accum.append(word)
//                        width += word.length
//                    }
//                }
//            } else { // fits as is, without need to wrap text
//                accum.append(text)
//                width += text.length
//            }
        }

        override fun toString(): String {
            return accum.toString()
        }

        companion object {
            private const val maxWidth = 80
        }
    }
}