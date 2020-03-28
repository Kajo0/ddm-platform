package pl.edu.pw.ddm.platform.core.data;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

interface DataLoader {

    String load(String uri, boolean deductType);

    String load(MultipartFile file, boolean deductType);

    LocalDataLoader.DataDesc getDataDesc(String datasetId);

    Map<String, LocalDataLoader.DataDesc> allDataInfo();

}
