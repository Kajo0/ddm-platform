export interface AlgorithmConfig {
  [key: string]: AlgorithmInfo;
}

export interface AlgorithmInfo {
  id: string;
  originalName: string;
  packageName: string;
  pipeline: string;
  algorithmType: string;
  algorithmName: string;
  sizeInBytes: number;
  location: string;
}
