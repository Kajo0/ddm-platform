package pl.edu.pw.ddm.platform.core.data;

interface DataLoader {

    String load(String uri, boolean deductType);

    LocalDataLoader.DataDesc getDataDesc(String datasetId);

}
