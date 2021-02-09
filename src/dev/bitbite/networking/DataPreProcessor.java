package dev.bitbite.networking;

import java.util.ArrayList;

/**
 * Keeps track of all {@link DataProcessingLayer}s at processes
 * data by passing it to each layer and returning its result.
 * Layers will process the data in the order they are registered.
 * 
 * @see DataProcessingLayer
 * 
 * @version 0.0.1-alpha
 */
public class DataPreProcessor {

	private ArrayList<DataProcessingLayer> layers;
	
	/**
	 * Instatiates the DataPreProcessor
	 */
	protected DataPreProcessor() {
		layers = new ArrayList<DataProcessingLayer>();
	}
	
	/**
	 * Sends the data to each registered {@link DataProcessingLayer} 
	 * If no DataProcessingLayer is registered, data will stay as it is
	 * and returns the processed data
	 * 
	 * @param data to process
	 * @return processed data
	 * 
	 * @version 0.0.1-alpha
	 */
	protected String process(String data) {
		for(DataProcessingLayer layer : layers) {
			data = layer.process(data);
		}
		return data;
	}
	
	/**
	 * Appends a layer at the end of the list
	 * @param layer to add
	 */
	protected void addLayer(DataProcessingLayer layer) {
		layers.add(layer);
	}
	
	/**
	 * Removes a layer from the list
	 * @param layer to remove
	 */
	protected void removeLayer(DataProcessingLayer layer) {
		layers.remove(layer);
	}
	
	/**
	 * Returns the list of layers
	 * @return ArrayList of {@link DataProcessingLayer}s
	 */
	protected ArrayList<DataProcessingLayer> getLayers(){
		return this.layers;
	}
	
}
