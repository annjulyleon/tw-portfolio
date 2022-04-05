package ru.inforion.soapui

import com.eviware.soapui.support.XmlHolder

class Utils {

    def context
    def log
    def runner

    def sortString(String input) {
        try {
            char[] charArray = input.replaceAll("[^а-яА-Яa-zA-Z0-9 ]+","").toCharArray()
            Arrays.sort(charArray)
            String sortedString = new String(charArray).trim()

            return sortedString.trim()
        }
        catch (NullPointerException e) {
            log.error (" Не указана строка для сортировки ", e)
            assert false
        }
    }

    def getRandomFromList(inputArray) {
        try {
            Random gen = new Random();
            int randomIndex = gen.nextInt(inputArray.size);

            return inputArray[randomIndex]
        }
        catch (IllegalArgumentException e) {
            log.error (e)
            assert false
        }
        catch (NullPointerException e) {
            log.error (e)
            assert false
        }
    }

    def translit(String message){
        try {
            char[] abcCyr = ['а','б','в','г','д','е','ё','ж','з','и','й','к','л','м','н','о','п','р','с','т','у','ф','х', 'ц','ч', 'ш','щ','ъ','ы','ь','э', 'ю','я','А','Б','В','Г','Д','Е','Ё', 'Ж','З','И','Й','К','Л','М','Н','О','П','Р','С','Т','У','Ф','Х', 'Ц', 'Ч','Ш', 'Щ','Ъ','Ы','Ь','Э','Ю','Я','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z']
            String[] abcLat = ["A","Б","B","Г","D","E","E","Ж","3","И","Й","K","Л","M","H","O","П","P","C","T","Y","Ф","Х","Ц","Ч","Ш","Щ", "Ъ","Ы", "Ь","Э","Ю","Я","A","Б","B","Г","D","E","E","Ж","3","И","Й","K","Л","M","H","O","П","P","C","T","Y","Ф","Х","Ц","Ч","Ш","Щ", "Ъ","Ы", "Ь","Э","Ю","Я","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"]
            StringBuilder builder = new StringBuilder()
            for (int i = 0; i < message.length(); i++) {
                for (int x = 0; x < abcCyr.length; x++ ) {
                    if (message.charAt(i) == abcCyr[x]) {
                        builder.append(abcLat[x])
                    }
                }
            }
            return builder.toString()
        }
        catch (NullPointerException e) {
            log.error (e)
            assert false
        }
    }

    /**
     * Generate random string.
     *
     * @param alphabet Specify alphabet array in format ('a'...'z').join(). For multiple: (('a'..'z')+('A'..'Z')+('а'..'я')).join().
     * @param n Number of characters in generated string.
     */
    def genString(String alphabet, int n) {
        try {
            String randomStr = new Random().with {
                (1..n).collect { alphabet[ nextInt( alphabet.length() ) ] }.join() }
            return randomStr
        }
        catch (MissingMethodException e) {
            log.error (e)
            assert false
        }
    }

    /**
     * Serialize given string to base64 and save to the specified Test Case property.
     * Specify runner in the constructor (either testRunner or messageExchange)
     * Method checks, whether it runs within Test Step or Script Assertion and behave accordingly.
     *
     * @param prop Name of Test Case property to save the result.
     * @param input Input string.
     */
    def base64This(String prop, String input) {
        try {
            String encoded = input.getBytes( 'UTF-8' ).encodeBase64()
            if (runner.getClass().getSimpleName() == "MockTestRunner") {
                runner.testCase.setPropertyValue("${prop}",encoded)
            }
            else {
                runner.modelItem.testStep.testCase.setPropertyValue("$prop",encoded)
            }
        }
        catch (NullPointerException e) {
            log.error (e)
            assert false
        }
    }

    //For assertions only
    /**
     * EGIS OTB format specific. Requires a field 'transferData' in the response.
     *
     * @return XmlHolder type, if there is another xml in the field. String type for everything else.
     */
    def getTransferData() {
        try {
            XmlHolder responseXmlHolder = new XmlHolder(runner.getResponseContentAsXml())
            String data = responseXmlHolder.getNodeValue("//*:transferData")
            if (data.contains("xml ver")) {
                XmlHolder dataXml = new XmlHolder(data)
                log.info ("XML found in transferData, passing as XmlHolder")
                return dataXml
            }
            else {
                log.info ("XML not found in transferData, passing as String")
                return data
            }
        }
        catch (GroovyRuntimeException e) {
            log.error (e)
            assert false
        }
    }
    /**
     * Method to simplify the comparison of sent and received base64 data.
     *
     * @param expectedData Name of the Test Case property, where sent data is stored
     * @return true or false
     */
    def compareBase64(String expectedData) {
        try {
        String response = new String(getTransferData().decodeBase64(), "UTF-8")
        def sent = new String(runner.modelItem.testStep.testCase
                .getPropertyValue("$expectedData")
                .decodeBase64(), "UTF-8")

        assert sortString(response) == sortString(sent), "Данные не совпадают"
        }
        catch (NullPointerException e) {
            log.error (e)
            assert false
        }

    }

}
