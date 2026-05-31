# WordAutoComplete

A desktop Java application that listens to your typing globally and offers word and phrase autocompletions based on frequency. It can auto-type the selected suggestion into any application.

## Features
- Global key listening (works across apps)
- Frequency-based word/phrase suggestions
- Quick selection via number keys
- Optional trailing space after autocompletion
- Persisted custom vocabulary (`userWords.json`)

## Project Structure
- src/main/java/org.example
    - AutoTyper — simulates typing/replacements in the active window
    - Controller — wires UI and logic
    - FrequencyTree — frequency-based suggestion engine
    - GlobalKeyLogger — captures keystrokes, updates suggestions
    - View — UI to display suggestions and controls
- resources — app resources
- top10000de.txt — seed word list (example)
- userWords.json — persisted user words
- pom.xml — Maven configuration

## Requirements
- Java 21+
- Maven 3.9+
- OS: Windows 11 (tested)

## Run
- Run the app in the IDE using `Controller.java` as the main class.

Note: The app uses a global keyboard hook. On first run, your OS or antivirus might prompt for permissions.

## Usage
- Start the app; the suggestion window should appear.
- Type anywhere; suggestions update as you type.
- Press number keys to accept a suggestion:
    - 1..9 select the corresponding suggestion
    - 0 will autocomplete the first word and add the string of the previous word combined with the autocompleted word as a new string to `userWords.json`.
- Space confirms a word into the internal vocabulary.
- There is an option to automatically add a space after autocompletion.

Tip: The vocabulary grows as you type; suggestions improve over time.

## Configuration
- Number of suggestions: adjustable in the UI or controller settings.
- Add space after autocompletion: toggleable.
- Other Configurations can be found in the Settings menu of the UI. They are currently not persistent.
- Seed word list: customize `top10000de.txt`.
- User vocabulary file: `userWords.json` (generated/updated at runtime). You can also add your own custom words here.

## Development
- Open in IntelliJ IDEA.
- JDK: 21
- Build/Run via Maven or IDE run configuration.

## Security and Privacy
- Keystrokes are processed locally to provide suggestions.
- `userWords.json` will save custom words for autocompletion. So be aware that also passwords and other sensitive data might be stored there. So make sure to use the pause button in such a situation. But it will only be stored locally and can be easily deleted.
- Be mindful of running global key loggers; only use software you trust.
- Review and adjust antivirus/exceptions if necessary.

## Troubleshooting
- No suggestions appear:
    - Ensure the app has permission for global keyboard hooks.
    - Check console logs for hook initialization errors.
- Hotkeys don’t insert text:
    - Some apps may block programmatic typing; try another target app.
    - Verify OS input permissions.
- Build failures:
    - Confirm Java 21 is selected: `java -version`
    - Run `mvn -v` to verify Maven.

## Credits
- Built with Java and Maven.
- Uses a global keyboard hook library to capture keystrokes.
- https://github.com/signalwerk/password/blob/master/wordlists/top10000de.txt for the list of words in `top10000de.txt`.