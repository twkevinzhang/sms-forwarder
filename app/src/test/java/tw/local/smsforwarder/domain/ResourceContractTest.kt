package tw.local.smsforwarder.domain

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class ResourceContractTest {
    private fun entries(folder: String): Map<String, List<String>> {
        val parser = DocumentBuilderFactory.newInstance()
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        val root =
            parser
                .newDocumentBuilder()
                .parse(File("src/main/res/$folder/strings.xml"))
                .documentElement
        return (0 until root.childNodes.length)
            .mapNotNull {
                val node = root.childNodes.item(it)
                if (node.nodeName !in setOf("string", "plurals")) null
                else {
                    val values =
                        if (node.nodeName == "string") listOf(node.textContent)
                        else
                            (0 until node.childNodes.length)
                                .map { index -> node.childNodes.item(index) }
                                .filter { item -> item.nodeName == "item" }
                                .map { item -> item.textContent }
                    node.attributes.getNamedItem("name").nodeValue to values
                }
            }
            .toMap()
    }

    @Test
    fun allLanguagesHaveIdenticalKeysAndFormattingArguments() {
        val english = entries("values")
        val traditional = entries("values-b+zh+Hant")
        assertEquals(english.keys, traditional.keys)
        val arguments = Regex("%(?:[0-9]+\\$)?[ds]")
        english.forEach { (key, values) ->
            assertTrue("Empty resource: $key", values.all { it.isNotBlank() })
            assertEquals("Plural variants: $key", values.size, traditional.getValue(key).size)
            values.zip(traditional.getValue(key)).forEach { (en, zh) ->
                assertEquals(
                    "Formatting: $key",
                    arguments.findAll(en).map { it.value }.toList(),
                    arguments.findAll(zh).map { it.value }.toList(),
                )
            }
        }
        Code.entries.forEach {
            assertTrue("Missing notice: $it", english.containsKey("notice_${it.name.lowercase()}"))
        }
    }
}
