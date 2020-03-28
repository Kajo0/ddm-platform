package pl.edu.pw.ddm.platform.core.data;

import java.io.File;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

interface DataLoader {

    String save(String uri, boolean deductType);

    String save(MultipartFile file, boolean deductType);

    File load(String dataId);

    // TODO move DataDesc to Interface
    LocalDataLoader.DataDesc getDataDesc(String datasetId);

    Map<String, LocalDataLoader.DataDesc> allDataInfo();

}
