package main;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String [] argvs) throws InterruptedException, SQLException, XMLStreamException, IOException {
        new GetData().run();
    }
}
