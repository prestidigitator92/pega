package pega.command.builder;

import pega.command.Command;
import pega.command.MergeCommand;
import pega.command.SortCommand;
import pega.io.FileInputProvider;
import pega.io.FileIterableOutputProvider;
import pega.io.FileOutputProvider;
import pega.util.TmpFileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MergeSortCommandsBuilder {

    private int maxMemory;
    private File inputFile;
    private File outputFile;
    private int inputSize;
    private TmpFileProvider tmpFileProvider;

    public MergeSortCommandsBuilder(int maxMemory, File inputFile, File outputFile, int inputSize, TmpFileProvider tmpFileProvider) {
        this.maxMemory = maxMemory;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.inputSize = inputSize;
        this.tmpFileProvider = tmpFileProvider;
    }

    public List<Command> build() {
        ArrayList<Command> commands = new ArrayList<>();

        int partsToSort = (int) Math.ceil((double)inputSize / (double)maxMemory);

        if (partsToSort == 1) {
            commands.add(new SortCommand(
                new FileInputProvider(
                    inputFile,
                    0,
                    inputSize
                ),
                new FileOutputProvider(outputFile)
            ));
        } else {
            List<Command> sortCommands = new ArrayList<>();

            for (int index = 0; index < partsToSort; index++) {
                int startPosition = index * maxMemory;
                int inputSize = this.inputSize - (index * maxMemory);
                if (inputSize > maxMemory) {
                    inputSize = maxMemory;
                }

                sortCommands.add(new SortCommand(
                    new FileInputProvider(
                        inputFile,
                        startPosition,
                        inputSize
                    ),
                    new FileOutputProvider(tmpFileProvider.create())
                ));
            }

            commands.addAll(sortCommands);
            commands.addAll(createMergeCommands(sortCommands));
        }


        return commands;
    }

    private List<Command> createMergeCommands(List<Command> input) {
        List<Command> commands = new ArrayList<>();

        if (input.size() == 2) {
            commands.add(new MergeCommand(
                input.get(0).getResult(),
                input.get(1).getResult(),
                new FileIterableOutputProvider(outputFile)
            ));
        } else {
            for (int i = 0; i < input.size() / 2; i++) {
                commands.add(new MergeCommand(
                    input.get(i).getResult(),
                    input.get(i + 1).getResult(),
                    new FileIterableOutputProvider(tmpFileProvider.create())
                ));
            }

            List<Command> toMerge;
            if (input.size() % 2 == 1) {
                toMerge = new ArrayList<>(commands);
                toMerge.add(input.get(input.size() - 1));
            } else {
                toMerge = commands;
            }

            if (toMerge.size() > 1) {
                commands.addAll(createMergeCommands(toMerge));
            }
        }

        return commands;
    }
}
