package main.java;

/**
 * Created by a.leonova on 30.06.2017.
 */

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ChangeDate {
    public static String dateCalculation(int modifier) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, modifier);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String calcDate = dateFormat.format(cal.getTime());
        return calcDate;
    }
    public static void main(String args[]) {

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new File(args[0]));
            NodeList nodeList1 = document.getElementsByTagName("actualPeriod");
            for(int i=0,size= nodeList1.getLength(); i<size; i++) {
                String actualFrom = nodeList1.item(i).getAttributes().getNamedItem("from").getNodeValue();
                String actualTo = nodeList1.item(i).getAttributes().getNamedItem("to").getNodeValue();

                String[] dateTime1 = actualFrom.split("T");
                int modifier1Int = Integer.parseInt(dateTime1[0]);
                String[] dateTime2 = actualTo.split("T");
                int modifier2Int = Integer.parseInt(dateTime2[0]);

                String date1 = dateCalculation(modifier1Int);
                String date2 = dateCalculation(modifier2Int);

                nodeList1.item(i).getAttributes().getNamedItem("from").setTextContent(date1 + "T" + dateTime1[1]);
                nodeList1.item(i).getAttributes().getNamedItem("to").setTextContent(date2 + "T" + dateTime2[1]);
            }
            NodeList nodeList2 = document.getElementsByTagName("routePoint");

            for(int i=0,size= nodeList2.getLength(); i<size; i++) {
                if (nodeList2.item(i).getAttributes().getNamedItem("departTime") != null && nodeList2.item(i).getAttributes().getNamedItem("departTime").getNodeValue() != null) {
                    String departTime = nodeList2.item(i).getAttributes().getNamedItem("departTime").getNodeValue();
                    String[] dateTime3 = departTime.split("T");
                    int modifier3Int = Integer.parseInt(dateTime3[0]);
                    String date3 = dateCalculation(modifier3Int);
                    nodeList2.item(i).getAttributes().getNamedItem("departTime").setTextContent(date3 + "T" + dateTime3[1]);
                } else {
                    continue;
                }
                if (nodeList2.item(i).getAttributes().getNamedItem("arriveTime") != null && nodeList2.item(i).getAttributes().getNamedItem("arriveTime").getNodeValue() != null) {
                    String arriveTime = nodeList2.item(i).getAttributes().getNamedItem("arriveTime").getNodeValue();
                    String[] dateTime4 = arriveTime.split("T");
                    int modifier4Int = Integer.parseInt(dateTime4[0]);
                    String date4 = dateCalculation(modifier4Int);
                    nodeList2.item(i).getAttributes().getNamedItem("arriveTime").setTextContent(date4 + "T" + dateTime4[1]);
                } else {
                    continue;
                }
            }

            NodeList nodeList3 = document.getElementsByTagName("imp:Import");
            String createdAt = nodeList3.item(0).getAttributes().getNamedItem("createdAt").getNodeValue();
            String[] dateTimeCreatedAt = createdAt.split("T");
            String dateCreatedAt = dateCalculation(0);
            nodeList3.item(0).getAttributes().getNamedItem("createdAt").setTextContent(dateCreatedAt + "T" + dateTimeCreatedAt[1]);

            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(args[1]));
            transformer.transform(source, result);
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}