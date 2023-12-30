export interface DataConfig {
  [key: string]: DataInfo;
}

export interface DistanceFunctionConfig {
  [key: string]: DistanceFunction;
}

export interface PartitionStrategyConfig {
  [key: string]: PartitionStrategy;
}

export interface DataInfo {
  id: string;
  originalName: string;
  type: string;
  sizeInBytes: number;
  numberOfSamples: number;
  separator: string;
  idIndex: number;
  labelIndex: number;
  attributesAmount: number;
  colTypes: string[];
  location: DataLocation;
}

export interface DataLocation {
  filesLocations: string[];
  sizesInBytes: number;
  numbersOfSamples: number;
}

export interface DistanceFunction {
  id: string;
  originalName: string;
  packageName: string;
  functionName: string;
  sizeInBytes: number;
  location: string;
}

export interface PartitionStrategy {
  id: string;
  originalName: string;
  packageName: string;
  strategyName: string;
  sizeInBytes: number;
  location: string;
}
