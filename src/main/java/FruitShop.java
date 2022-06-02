import csv.FileServiceImpl;
import dao.ProductAccountDaoImpl;
import db.Storage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ProductAccount;
import service.AmountHandler;
import service.AmountHandlerBalance;
import service.AmountHandlerPurchase;
import service.AmountHandlerReturn;
import service.AmountHandlerSupply;
import service.HeaderParts;
import service.Operation;
import service.OperationHandlerStrategyImpl;
import service.ShopReportService;
import service.ShopReportServiceImpl;
import service.ShopServiceImpl;

public class FruitShop {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("you should to use : FruitShop.jar [input.csv] [output.csv]");
            return;
        }

        //initialize map for AmountStrategy
        Map<Operation,AmountHandler> amountHandlerMap = new HashMap<Operation, AmountHandler>();
        amountHandlerMap.put(Operation.BALANCE,new AmountHandlerBalance());
        amountHandlerMap.put(Operation.SUPPLY,new AmountHandlerSupply());
        amountHandlerMap.put(Operation.PURCHASE,new AmountHandlerPurchase());
        amountHandlerMap.put(Operation.RETURN,new AmountHandlerReturn());

        OperationHandlerStrategyImpl amountStrategy =
                new OperationHandlerStrategyImpl(amountHandlerMap);

        //initialize storage
        Storage memdb = new Storage();

        ProductAccountDaoImpl dao = new ProductAccountDaoImpl(memdb);
        ShopServiceImpl fruitShop = new ShopServiceImpl(dao, amountStrategy);
        ShopReportService reportService = new ShopReportServiceImpl(dao);

        //Start read input csv record by record

        //CsvReadProcessor csvProcessorRead = new CsvReadProcessor();
        //csvProcessorRead.openCsv(inputCsvFilePath);
        //
        FileServiceImpl csvFileService = new FileServiceImpl();

        String inputCsvFilePath = args[0];
        List<String> inputList = csvFileService.readFile(inputCsvFilePath);

        for (String inCsvString: inputList) {
            String[] inArray = inCsvString.split(",");
            fruitShop.productTransaction(inArray[1],Double.valueOf(inArray[2]),inArray[0]);
        }

        /*    do {
            String fruitName = csvProcessorRead.getField(HeaderParts.FRUIT.name());
            Double quantity
            = Double.parseDouble(csvProcessorRead.getField(HeaderParts.QUANTITY.name()));
            String opType = csvProcessorRead.getField(HeaderParts.TYPE.name());

            fruitShop.productTransaction(fruitName,quantity, opType);
        } while (csvProcessorRead.readNext());
        */

        ArrayList<ProductAccount> balanceReport;
        balanceReport = (ArrayList<ProductAccount>)reportService.getShopBalanceReport();

        List<String> outStringList = new ArrayList<String>();
        outStringList.add((HeaderParts.FRUIT.name().toLowerCase()
                + ","
                + HeaderParts.QUANTITY.name().toLowerCase()));
        for (ProductAccount productAccount:balanceReport) {
            outStringList.add((productAccount.getName()
                    + ","
                    + productAccount.getAmount().toString()));
        }

        String outCsvFilePath = args[1];
        csvFileService.writeFile(outCsvFilePath,outStringList);

        //prepare csv object to write
        /*
        CsvWriteProcessor csvProcessorWrite = new CsvWriteProcessor();
        csvProcessorWrite.createCsv(outCsvFilePath,
                new String[]{HeaderParts.FRUIT.name(),
                HeaderParts.QUANTITY.name()});
        for (ProductAccount productAccount:balanceReport) {
            csvProcessorWrite.setField(HeaderParts.FRUIT.name(),productAccount.getName());
            csvProcessorWrite.setField(HeaderParts.QUANTITY.name(),
                    productAccount.getAmount().toString());
            csvProcessorWrite.write();
        } */
    }
}
