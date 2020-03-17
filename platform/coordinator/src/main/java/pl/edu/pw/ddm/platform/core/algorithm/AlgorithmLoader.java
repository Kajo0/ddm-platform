package pl.edu.pw.ddm.platform.core.algorithm;

interface AlgorithmLoader {

    String load(String url);

    String load(String name, byte[] jar);

    // FIXME move interface class
    InMemoryAlgorithmLoader.AlgorithmDesc getAlgorithm(String algorithmId);

}
