package dev.bitbite.networking;

import java.util.ArrayList;

import dev.bitbite.networking.exceptions.LayerDisableFailedException;
import dev.bitbite.networking.exceptions.LayerInitFailedException;

/**
 * Keeps track of all {@link DataProcessingLayer}s and processes
 * data by passing it to each layer and returning its result.
 * Layers will process the data in the order they are registered.
 * 
 * @see DataProcessingLayer
 */
public class DataPreProcessor {

	private ArrayList<DataProcessingLayer> incomingDataProcessingLayers;
	private ArrayList<DataProcessingLayer> outgoingDataProcessingLayers;
	
	/**
	 * The Transfer directions data can travel
	 */
	public enum TransferMode {
		/**
		 * Data is incoming
		 */
		IN, 
		/**
		 * Data is outgoing
		 */
		OUT;
	}
	
	/**
	 * Instantiates the DataPreProcessor
	 */
	protected DataPreProcessor() {
		this.incomingDataProcessingLayers = new ArrayList<DataProcessingLayer>();
		this.outgoingDataProcessingLayers = new ArrayList<DataProcessingLayer>();
	}
	
	/**
	 * Sends the data to each registered {@link DataProcessingLayer} 
	 * If no DataProcessingLayer is registered, data will stay as it is
	 * and returns the processed data
	 * 
	 * @param mode - transfer direction of the data 
	 * @param data to process
	 * @return processed data
	 */
	protected byte[] process(TransferMode mode, byte[] data) {
		if(mode == TransferMode.IN) {
			for(DataProcessingLayer layer : this.incomingDataProcessingLayers) {
				data = layer.process(data);
			}
		} else if(mode == TransferMode.OUT) {
			for(DataProcessingLayer layer : this.outgoingDataProcessingLayers) {
				data = layer.process(data);
			}
		}
		return data;
	}
	
	/**
	 * Initializes the {@link DataProcessingLayer}s
	 * @throws LayerInitFailedException if {@link DataProcessingLayer#onEnable()} returns false
	 */
	public void initLayers() throws LayerInitFailedException {
		for(DataProcessingLayer l : this.incomingDataProcessingLayers) {
			if(!l.onEnable()) throw new LayerInitFailedException(l.getClass().getName());
		}
		for(DataProcessingLayer l : this.outgoingDataProcessingLayers) {
			if(!l.onEnable()) throw new LayerInitFailedException(l.getClass().getName());
		}
	}
	
	/**
	 * Deactivates the {@link DataProcessingLayer}s
	 * @throws LayerDisableFailedException if {@link DataProcessingLayer#onDisable()} returns false
	 */
	public void shutdown() throws LayerDisableFailedException {
		for(DataProcessingLayer l : this.incomingDataProcessingLayers) {
			if(!l.onDisable()) throw new LayerDisableFailedException(l.getClass().getName());
		}
		for(DataProcessingLayer l : this.outgoingDataProcessingLayers) {
			if(!l.onDisable()) throw new LayerDisableFailedException(l.getClass().getName());
		}
	}
	
	/**
	 * Appends a layer at the end of the list
	 * 
	 * @param mode - the transfermode the layer is designed for
	 * @param layer - {@link DataProcessingLayer} to add
	 */
	public void addLayer(TransferMode mode, DataProcessingLayer layer) {
		if(mode == TransferMode.IN) this.incomingDataProcessingLayers.add(layer);
		if(mode == TransferMode.OUT) this.outgoingDataProcessingLayers.add(layer);
	}

	/**
	 * Inserts a layer at the specified index. Shifts the layer currently at that position (if any) and
	 * any subsequent layers to the right (adds one to their indices).
	 * 
	 * @param mode - the transfermode the layer is designed for
	 * @param index to add the layer at
	 * @param layer - {@link DataProcessingLayer} to add
	 */
	public void addLayer(TransferMode mode, int index, DataProcessingLayer layer) {
		if(mode == TransferMode.IN) this.incomingDataProcessingLayers.add(index, layer);
		if(mode == TransferMode.OUT) this.outgoingDataProcessingLayers.add(index, layer);
	}
	
	/**
	 * Returns a layer of the specified transfermode at a specified position in the list.
	 * @param mode - the transfermode of the layer to get
	 * @param index of the layer to get
	 * @return layer - {@link DataProcessingLayer} at given index
	 */
	public DataProcessingLayer getLayerAt(TransferMode mode, int index) {
		if(mode == TransferMode.IN) return this.incomingDataProcessingLayers.get(index);
		if(mode == TransferMode.OUT) return this.outgoingDataProcessingLayers.get(index);
		return null;
	}
	
	/**
	 * Removes a layer from the list
	 * @param mode - the transfermode of the layer to remove
	 * @param layer - {@link DataProcessingLayer} to remove
	 */
	public void removeLayer(TransferMode mode, DataProcessingLayer layer) {
		if(mode == TransferMode.IN) this.incomingDataProcessingLayers.remove(layer);
		if(mode == TransferMode.OUT) this.outgoingDataProcessingLayers.remove(layer); 
	}
	
	/**
	 * Returns the list of layers
	 * @param mode - the transfermode of the layer to remove
	 * @return ArrayList of {@link DataProcessingLayer}s
	 */
	public ArrayList<DataProcessingLayer> getLayers(TransferMode mode) {
		if(mode == TransferMode.IN) return this.incomingDataProcessingLayers;
		if(mode == TransferMode.OUT) return this.outgoingDataProcessingLayers;
		return null;
	}
	
}
