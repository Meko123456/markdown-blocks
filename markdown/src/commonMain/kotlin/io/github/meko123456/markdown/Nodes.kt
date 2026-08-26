package io.github.meko123456.markdown

/**
 * Inline formatting inside a line of text.
 *
 * These are deliberately render-agnostic: map them to a Compose `AnnotatedString`,
 * a SwiftUI `Text`, HTML, or anything else you like.
 */
public sealed interface MdSpan {
    /** Plain, unformatted text. */
    public data class Text(val text: String) : MdSpan

    /** `**bold**` */
    public data class Bold(val text: String) : MdSpan

    /** `*italic*` or `_italic_` */
    public data class Italic(val text: String) : MdSpan

    /** `` `code` `` */
    public data class Code(val text: String) : MdSpan

    /** `~~struck~~` */
    public data class Strikethrough(val text: String) : MdSpan

    /** `[label](destination)` */
    public data class Link(val text: String, val destination: String) : MdSpan
}

/** A block-level element of a parsed markdown document. */
public sealed interface MdBlock {
    /** `# Heading` — [level] is 1..6. */
    public data class Heading(val level: Int, val spans: List<MdSpan>) : MdBlock

    /** A run of text separated from its neighbours by a blank line. */
    public data class Paragraph(val spans: List<MdSpan>) : MdBlock

    /** `- item` / `* item`. */
    public data class BulletItem(val spans: List<MdSpan>) : MdBlock

    /** `1. item` — [number] is the literal number written in the source. */
    public data class OrderedItem(val number: Int, val spans: List<MdSpan>) : MdBlock

    /** `> quoted` */
    public data class Quote(val spans: List<MdSpan>) : MdBlock

    /** A fenced ``` block; [language] is the info string, if any. */
    public data class CodeBlock(val code: String, val language: String? = null) : MdBlock

    /** `---`, `***` or `___` */
    public data object Divider : MdBlock
}
