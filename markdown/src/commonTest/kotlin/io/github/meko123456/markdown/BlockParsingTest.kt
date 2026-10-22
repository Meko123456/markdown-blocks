package io.github.meko123456.markdown

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BlockParsingTest {

    @Test
    fun parsesHeadingLevels() {
        val blocks = Markdown.parse("# One\n\n## Two\n\n###### Six")
        assertEquals(1, (blocks[0] as MdBlock.Heading).level)
        assertEquals(2, (blocks[1] as MdBlock.Heading).level)
        assertEquals(6, (blocks[2] as MdBlock.Heading).level)
    }

    @Test
    fun sevenHashesIsNotAHeading() {
        assertIs<MdBlock.Paragraph>(Markdown.parse("####### too deep").single())
    }

    @Test
    fun hashWithoutSpaceIsNotAHeading() {
        assertIs<MdBlock.Paragraph>(Markdown.parse("#hashtag").single())
    }

    @Test
    fun parsesBulletMarkers() {
        val blocks = Markdown.parse("- dash\n* star\n+ plus")
        assertEquals(3, blocks.size)
        assertTrue(blocks.all { it is MdBlock.BulletItem })
    }

    @Test
    fun parsesOrderedItemsKeepingTheirNumbers() {
        val blocks = Markdown.parse("1. first\n2. second\n10) tenth")
        assertEquals(1, (blocks[0] as MdBlock.OrderedItem).number)
        assertEquals(2, (blocks[1] as MdBlock.OrderedItem).number)
        assertEquals(10, (blocks[2] as MdBlock.OrderedItem).number)
    }

    @Test
    fun parsesQuote() {
        val quote = Markdown.parse("> to be").single()
        assertEquals(listOf(MdSpan.Text("to be")), (quote as MdBlock.Quote).spans)
    }

    @Test
    fun consecutiveQuoteLinesAreOneQuote() {
        val quote = Markdown.parse("> The best time to plant a tree\n> was twenty years ago.").single()
        assertEquals(
            listOf(MdSpan.Text("The best time to plant a tree was twenty years ago.")),
            (quote as MdBlock.Quote).spans,
        )
    }

    @Test
    fun aBareQuoteMarkerSeparatesQuotesInsteadOfMakingAnEmptyOne() {
        val blocks = Markdown.parse("> first\n>\n> second")
        assertEquals(
            listOf(MdBlock.Quote(listOf(MdSpan.Text("first"))), MdBlock.Quote(listOf(MdSpan.Text("second")))),
            blocks,
        )
        assertEquals(emptyList(), Markdown.parse(">"))
    }

    @Test
    fun aQuoteEndsAtABlankLine() {
        val blocks = Markdown.parse("before\n> quoted\n\nafter\n\n> again")
        assertEquals(
            listOf(
                MdBlock.Paragraph(listOf(MdSpan.Text("before"))),
                MdBlock.Quote(listOf(MdSpan.Text("quoted"))),
                MdBlock.Paragraph(listOf(MdSpan.Text("after"))),
                MdBlock.Quote(listOf(MdSpan.Text("again"))),
            ),
            blocks,
        )
    }

    /** CommonMark's laziness: the lines after a quote's first may leave out the ">". */
    @Test
    fun aPlainLineStraightAfterAQuoteContinuesIt() {
        assertEquals(
            listOf(MdBlock.Quote(listOf(MdSpan.Text("The best time to plant a tree was twenty years ago.")))),
            Markdown.parse("> The best time to plant a tree\nwas twenty years ago."),
        )
        // Marked and unmarked lines can alternate, and it is still one quote.
        assertEquals(
            listOf(MdBlock.Quote(listOf(MdSpan.Text("one two three")))),
            Markdown.parse("> one\ntwo\n> three"),
        )
    }

    /**
     * Only paragraph text is lazy. A list item, a heading or a rule after a quote is a block of its
     * own, as in CommonMark, and so is a line after a bare ">", which has already ended the quote's
     * paragraph.
     */
    @Test
    fun onlyParagraphTextContinuesAQuote() {
        val quoted = MdBlock.Quote(listOf(MdSpan.Text("quoted")))
        assertEquals(listOf(quoted, MdBlock.BulletItem(listOf(MdSpan.Text("item")))), Markdown.parse("> quoted\n- item"))
        assertEquals(listOf(quoted, MdBlock.Heading(2, listOf(MdSpan.Text("Next")))), Markdown.parse("> quoted\n## Next"))
        assertEquals(listOf(quoted, MdBlock.Divider), Markdown.parse("> quoted\n---"))
        assertEquals(listOf(quoted, MdBlock.Paragraph(listOf(MdSpan.Text("after")))), Markdown.parse("> quoted\n>\nafter"))
    }

    @Test
    fun parsesFencedCodeWithLanguage() {
        val code = Markdown.parse("```kotlin\nval x = 1\n```").single() as MdBlock.CodeBlock
        assertEquals("kotlin", code.language)
        assertEquals("val x = 1", code.code)
    }

    @Test
    fun fencedCodeWithoutLanguageHasNullLanguage() {
        val code = Markdown.parse("```\nplain\n```").single() as MdBlock.CodeBlock
        assertEquals(null, code.language)
    }

    @Test
    fun markdownInsideCodeIsNotParsed() {
        val code = Markdown.parse("```\n# not a heading\n- not a bullet\n```").single() as MdBlock.CodeBlock
        assertEquals("# not a heading\n- not a bullet", code.code)
    }

    @Test
    fun unterminatedFenceStillProducesACodeBlock() {
        val code = Markdown.parse("```\nleft open").single() as MdBlock.CodeBlock
        assertEquals("left open", code.code)
    }

    @Test
    fun parsesDividerVariants() {
        val blocks = Markdown.parse("---\n\n***\n\n___")
        assertEquals(3, blocks.size)
        assertTrue(blocks.all { it === MdBlock.Divider })
    }

    @Test
    fun consecutiveLinesJoinIntoOneParagraph() {
        val blocks = Markdown.parse("line one\nline two\n\nsecond para")
        assertEquals(2, blocks.size)
        assertEquals(listOf(MdSpan.Text("line one line two")), (blocks[0] as MdBlock.Paragraph).spans)
    }

    @Test
    fun blankInputProducesNoBlocks() {
        assertTrue(Markdown.parse("").isEmpty())
        assertTrue(Markdown.parse("\n\n   \n").isEmpty())
    }

    @Test
    fun handlesWindowsLineEndings() {
        val blocks = Markdown.parse("# Title\r\n\r\nbody")
        assertEquals(2, blocks.size)
        assertIs<MdBlock.Heading>(blocks[0])
    }

    @Test
    fun parsesARealisticDocument() {
        val doc = """
            # Shopping

            Need to buy **milk** and `eggs`.

            - dairy
            - bread

            1. go to shop
            2. pay

            > don't forget the list

            ```kotlin
            val list = listOf("milk")
            ```

            ---
        """.trimIndent()
        val blocks = Markdown.parse(doc)
        assertIs<MdBlock.Heading>(blocks[0])
        assertIs<MdBlock.Paragraph>(blocks[1])
        assertIs<MdBlock.BulletItem>(blocks[2])
        assertIs<MdBlock.OrderedItem>(blocks[4])
        assertIs<MdBlock.Quote>(blocks[6])
        assertIs<MdBlock.CodeBlock>(blocks[7])
        assertEquals(MdBlock.Divider, blocks[8])
    }
}
