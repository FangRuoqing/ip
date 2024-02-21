package rochin;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represent a command processor for handling user commands related to tasks.
 */
public class CommandProcessor {
    private final String command;
    private boolean isExitCommand;

    /**
     * Construct a CommandProcessor with the given command.
     *
     * @param command The command to be processed.
     */
    public CommandProcessor(String command) {
        this.command = command;
    }

    /**
     * Process the command based on its operation.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void process(TaskList tasks, Ui ui) {
        if (!isExitCommand) {
            String[] splitCommand = command.split("\\s+");
            String operation = splitCommand[0].toLowerCase();

            switch (operation) {
                case "list":
                    ui.showTaskList(tasks.getAllTasks());
                    break;
                case "todo":
                    processTodoCommand(tasks, ui);
                    break;
                case "deadline":
                    processDeadlineCommand(tasks, ui);
                    break;
                case "event":
                    processEventCommand(tasks, ui);
                    break;
                case "delete":
                    processDeleteCommand(tasks, ui);
                    break;
                case "mark":
                    processMarkCommand(tasks, ui);
                    break;
                case "unmark":
                    processUnmarkCommand(tasks, ui);
                    break;
                case "find":
                    processFindCommand(tasks, ui);
                    break;
                default:
                    ui.showUnknownCommandError();
            }
        }
    }

    /**
     * Process a "todo" command and adds a new todo task to the TaskList.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void processTodoCommand(TaskList tasks, Ui ui) {
        try {
            String description = command.substring("todo".length()).trim();
            if (description.isEmpty()) {
                throw new RochinException("OOPS!!! The description of a todo cannot be empty.");
            }
            tasks.addTask(new TodoTask(description));
            ui.showTaskAddedMessage(tasks.getAllTasks());
        } catch (RochinException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Process a "deadline" command and adds a new deadline task to the TaskList.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void processDeadlineCommand(TaskList tasks, Ui ui) {
        try {
            String descriptionWithDate = command.substring("deadline".length()).trim();
            // Splitting the description and deadline by "/by"
            String[] parts = descriptionWithDate.split("/by");
            if (parts.length != 2) {
                throw new RochinException("OOPS!!! Please provide both a description and a deadline for a deadline task.");
            }
            String description = parts[0].trim();
            String deadline = parts[1].trim();
            // Parse the deadline string to LocalDateTime
            LocalDateTime deadlineDateTime = DateAndTime.parseDateTime(deadline);
            DeadlineTask ddlTask = new DeadlineTask(description, deadlineDateTime);
            tasks.addTask(ddlTask);
            ui.showTaskAddedMessage(tasks.getAllTasks());
        } catch (RochinException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Process an "event" command and adds a new event task to the TaskList.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void processEventCommand(TaskList tasks, Ui ui) {
        try {
            String descriptionWithDate = command.substring("event".length()).trim();
            // Splitting the description, starting datetime, and ending datetime by "/from" and "/to"
            String[] parts = descriptionWithDate.split("/from");
            if (parts.length != 2) {
                throw new RochinException("OOPS!!! Please provide a description, start time, and end time for an event task.");
            }
            String description = parts[0].trim();
            String[] dateTimeParts = parts[1].split("/to");
            if (dateTimeParts.length != 2) {
                throw new RochinException("OOPS!!! Please provide both starting and ending date/time for the event.");
            }
            String fromDateTime = dateTimeParts[0].trim();
            String toDateTime = dateTimeParts[1].trim();
            // Parse the starting and ending date/time strings to LocalDateTime
            LocalDateTime fromDateTimeObject = DateAndTime.parseDateTime(fromDateTime);
            LocalDateTime toDateTimeObject = DateAndTime.parseDateTime(toDateTime);
            EventTask eventTask = new EventTask(description, fromDateTimeObject, toDateTimeObject);
            tasks.addTask(eventTask);
            ui.showTaskAddedMessage(tasks.getAllTasks());
        } catch (RochinException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Process a "delete" command and deletes a task from the TaskList.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void processDeleteCommand(TaskList tasks, Ui ui) {
        int taskIndex = getTaskIndex();
        tasks.deleteTask(taskIndex);
        ui.showTaskDeletedMessage(tasks.getAllTasks());
    }

    /**
     * Process a "mark" command and marks a task as done in the TaskList.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void processMarkCommand(TaskList tasks, Ui ui) {
        int taskIndex = getTaskIndex();
        tasks.markTaskAsDone(taskIndex);
        ui.showTaskMarkedAsDoneMessage(tasks.getAllTasks());
    }

    /**
     * Process an "unmark" command and unmarks a task as done in the TaskList.
     *
     * @param tasks The TaskList to be modified.
     * @param ui    The user interface for displaying messages.
     */
    public void processUnmarkCommand(TaskList tasks, Ui ui) {
        int taskIndex = getTaskIndex();
        tasks.unmarkTaskAsDone(taskIndex);
        ui.showTaskUnmarkedAsDoneMessage(tasks.getAllTasks());
    }

    /**
     * Extract the task index from the command.
     *
     * @return The index of the task.
     */
    public int getTaskIndex() {
        try {
            String[] splitCommand = command.split("\\s+");
            return Integer.parseInt(splitCommand[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            Ui.showInvalidCommandError();
            return -1;
        }
    }

    /**
     * Process the "find" command, searching for tasks containing the specified keyword.
     *
     * @param tasks The TaskList containing all tasks.
     * @param ui    The Ui for displaying user interface messages.
     */
    public void processFindCommand(TaskList tasks, Ui ui) {
        try {
            String keyword = command.substring("find".length()).trim();
            if (keyword.isEmpty()) {
                throw new RochinException("OOPS!!! Please provide a keyword to search for.");
            }
            List<Task> matchingTasks = tasks.findTasks(keyword);
            ui.showTaskList(matchingTasks);
        } catch (RochinException e) {
            ui.showError(e.getMessage());
        }
    }

    /**
     * Check if the command is an exit command.
     *
     * @return True if the command is an exit command, false otherwise.
     */
    public boolean isExitCommand() {
        isExitCommand = command.equalsIgnoreCase("bye");
        return isExitCommand;
    }
}
