package autoencoder;

import org.encog.engine.network.activation.ActivationReLU;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.sgd.StochasticGradientDescent;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;

public class NeuralEncoder extends Encoder
{
    private BasicNetwork _network = new BasicNetwork();

    private final int ENCODER_LAYER     = 1;
    private final double LEARNING_RATE  = 1e-9; //was 10e-6 for CTB
    private final int BATCH_SIZE        = 2; //was 100 for CTB
    private final double TERMINAL_ERROR = 1.5; //was 0.025 CTB

    @Override
    public void build(final int encoder_size, final int input_size)
    {
        _network.addLayer(new BasicLayer(new ActivationReLU(), true, input_size));
        _network.addLayer(new BasicLayer(new ActivationReLU(), true, encoder_size));
        _network.addLayer(new BasicLayer(new ActivationReLU(), true, input_size));
        _network.getStructure().finalizeStructure();
        _network.reset();
        // Set network status to ready (permit evaluation) as well as reset size.
        _encoder_ready = true;
        _encoder_size  = encoder_size;
    }

    @Override
    public void build(final int encoder_size,
                      final int input_size,
                      final double[][] training_inputs,
                      final String save_filename)
    {
        // Build network with given input and encoder size.
        build(encoder_size, input_size);
        // Adapt network weights and store resulting parameters in save file.
        adapt(training_inputs);
        store(save_filename);
    }

    @Override
    public void build(final int encoder_size,
                      final int input_size,
                      final String load_filename)
    {
        // Build network with given input and encoder size.
        build(encoder_size, input_size);
        // Load network weights from load file.
        load(load_filename);
    }


    private void adapt(final double[][] training_inputs)
    {
        // Build training data set.
        final MLDataSet training_data = new BasicMLDataSet(training_inputs, training_inputs);
        // Train network (inputs = label as encoder).
        int epoch = 1; double last_error;
        StochasticGradientDescent train = new StochasticGradientDescent(_network, training_data);
        train.setLearningRate(LEARNING_RATE);
        train.setBatchSize(BATCH_SIZE);
        do {
            last_error = train.getError();
            train.iteration();
            System.out.printf("Epoch #%d, Error = %f, Grad = %f\n",
                              epoch, train.getError(), Math.abs(train.getError() - last_error));
            epoch++;
            if(epoch>5000){
                epoch = 0;
                _network.reset();
            }
        } while (Math.abs(train.getError()) > TERMINAL_ERROR);
        train.finishTraining();


        Backpropagation train_ = new Backpropagation(_network, training_data);
        train_.setLearningRate(LEARNING_RATE);
        train_.setBatchSize(BATCH_SIZE);
        do {
            last_error = train_.getError();
            train_.iteration();
            System.out.printf("Epoch #%d, Error = %f, Grad = %f\n",
                    epoch, train_.getError(), Math.abs(train_.getError() - last_error));
            epoch++;
        } while (Math.abs(train_.getError() - last_error) > 10e-4);
        train_.finishTraining();


    }

    @Override
    public double[] encoding(final int[] input)
    {
        double[] input_double = new double[input.length];
        for(int i=0; i < input.length; i++)
        {
            input_double[i] = input[i];
        }
        final MLData input_data = new BasicMLData(input_double);
        _network.compute(input_data);
        double[] encoded_layer = new double[_encoder_size];
        for (int k = 0; k < _encoder_size; ++k)
        {
            encoded_layer[k] = _network.getLayerOutput(ENCODER_LAYER, k);
        }
        return encoded_layer;
    }

    @Override
    public int flatten(final int[] input)
    {
        return -1;
    }

    private void store(final String filename)
    {
        EncogDirectoryPersistence.saveObject(new File(filename), _network);
    }

    private void load(final String filename)
    {
        _network = (BasicNetwork) EncogDirectoryPersistence.loadObject(new File(filename));
        // Set network status to ready (permit evaluation).
        _encoder_ready = true;
    }
}
