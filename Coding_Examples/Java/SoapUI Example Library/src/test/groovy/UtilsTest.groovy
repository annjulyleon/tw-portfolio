import spock.lang.Specification
import ru.inforion.soapui.*

class UtilsTest extends Specification {
    def tc = new Utils()

    def "String sorting"() {
       given:
            String str = tc.sortString("Некоторая строчка из different 4562 символов")
       expect:
            str == "2456deeffinrtНааввезиикклмоооооррссттчя"
   }

    def "Random from array"() {
        given:
            String str = tc.getRandomFromList(["1","2","3","4","5"])
        expect:
            ["1","2","3","4","5"].contains(str)
    }

    def "Transliteration"() {
        given:
            String str = tc.translit(('а'..'я').join())
        expect:
            str == "AБBГDEЖ3ИЙKЛMHOПPCTYФХЦЧШЩЪЫЬЭЮЯ"
    }

}
