package io.github.meko123456.markdown

import kotlin.test.Test
import kotlin.test.assertEquals

class InlineParsingTest {

    @Test
    fun parsesBold() =
        assertEquals(listOf(MdSpan.Bold("loud")), Markdown.parseInline("**loud**"))

    @Test
    fun parsesUnderscoreBold() =
        assertEquals(listOf(MdSpan.Bold("loud")), Markdown.parseInline("__loud__"))

    @Test
    fun parsesItalic() =
        assertEquals(listOf(MdSpan.Italic("soft")), Markdown.parseInline("*soft*"))

    @Test
    fun parsesUnderscoreItalic() =
        assertEquals(listOf(MdSpan.Italic("soft")), Markdown.parseInline("_soft_"))

    @Test
    fun parsesCode() =
        assertEquals(listOf(MdSpan.Code("val x")), Markdown.parseInline("`val x`"))

    @Test
    fun parsesStrikethrough() =
        assertEquals(listOf(MdSpan.Strikethrough("gone")), Markdown.parseInline("~~gone~~"))

    @Test
    fun parsesLink() =
        assertEquals(
            listOf(MdSpan.Link("Kotlin", "https://kotlinlang.org")),
            Markdown.parseInline("[Kotlin](https://kotlinlang.org)"),
        )

    @Test
    fun mixesSpansInOrder() {
        val spans = Markdown.parseInline("a **b** c *d* e `f`")
        assertEquals(MdSpan.Text("a "), spans[0])
        assertEquals(MdSpan.Bold("b"), spans[1])
        assertEquals(MdSpan.Italic("d"), spans[3])
        assertEquals(MdSpan.Code("f"), spans[5])
    }

    @Test
    fun boldWinsOverItalicForDoubleStars() =
        assertEquals(listOf(MdSpan.Bold("x")), Markdown.parseInline("**x**"))

    @Test
    fun codeContentIsNotFurtherParsed() =
        assertEquals(listOf(MdSpan.Code("**not bold**")), Markdown.parseInline("`**not bold**`"))

    @Test
    fun arithmeticIsNotMistakenForEmphasis() {
        // Emphasis can't open before whitespace or close after it (CommonMark flanking).
        assertEquals(listOf(MdSpan.Text("2 * 3 * 4 = 24")), Markdown.parseInline("2 * 3 * 4 = 24"))
    }

    @Test
    fun emphasisStillWorksWhenTightlyWrapped() =
        assertEquals(listOf(MdSpan.Italic("yes")), Markdown.parseInline("*yes*"))

    @Test
    fun unmatchedMarkerStaysLiteral() =
        assertEquals(listOf(MdSpan.Text("a * b")), Markdown.parseInline("a * b"))

    @Test
    fun danglingBacktickIsLiteral() =
        assertEquals(listOf(MdSpan.Text("a ` b")), Markdown.parseInline("a ` b"))

    @Test
    fun emptyEmphasisIsLiteral() =
        assertEquals(listOf(MdSpan.Text("****")), Markdown.parseInline("****"))

    @Test
    fun linkWithoutDestinationIsLiteral() =
        assertEquals(listOf(MdSpan.Text("[label]()")), Markdown.parseInline("[label]()"))

    @Test
    fun bracketsWithoutParensAreLiteral() =
        assertEquals(listOf(MdSpan.Text("[just brackets]")), Markdown.parseInline("[just brackets]"))

    @Test
    fun plainTextPassesThrough() =
        assertEquals(listOf(MdSpan.Text("nothing special")), Markdown.parseInline("nothing special"))

    @Test
    fun visibleTextSurvivesParsing() {
        // Whatever the markup, the words a reader sees must all still be there.
        val cases = mapOf(
            "**a** *b* `c` ~~d~~" to "a b c d",
            "see [Kotlin](https://kotlinlang.org) now" to "see Kotlin now",
            "* unmatched" to "* unmatched",
            "trailing **" to "trailing **",
            "plain words only" to "plain words only",
        )
        for ((input, expected) in cases) {
            val rendered = Markdown.parseInline(input).joinToString("") { visibleText(it) }
            assertEquals(expected, rendered, "visible text changed for: $input")
        }
    }

    /** What a reader would actually see — link destinations are not displayed. */
    private fun visibleText(span: MdSpan): String = when (span) {
        is MdSpan.Text -> span.text
        is MdSpan.Bold -> span.text
        is MdSpan.Italic -> span.text
        is MdSpan.Code -> span.text
        is MdSpan.Strikethrough -> span.text
        is MdSpan.Link -> span.text
    }
}
