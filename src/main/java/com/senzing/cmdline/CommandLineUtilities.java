package com.senzing.cmdline;

import com.senzing.util.JsonUtils;
import com.senzing.util.LoggingUtilities;

import javax.json.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Pattern;

import static com.senzing.util.LoggingUtilities.*;
import static com.senzing.cmdline.CommandLineSource.*;

/**
 * Utility functions for parsing command line arguments.
 *
 */
public class CommandLineUtilities {
  /***
   * The Regular Expression pattern for JSON arrays.
   */
  private static final Pattern JSON_ARRAY_PATTERN
      = Pattern.compile(
          "\\s*\\[\\s*((\\\".*\\\"\\s*,\\s*)+"
          + "(\\\".*\\\")|(\\\".*\\\")?)\\s*\\]\\s*");

  /**
   * The JAR file name containing this class.
   */
  public static final String JAR_FILE_NAME;

  /**
   * The base URL of the JAR file containing this class.
   */
  public static final String JAR_BASE_URL;

  /**
   * The URL path to the JAR file containing this class.
   */
  public static final String PATH_TO_JAR;

  static {
    String jarBaseUrl = null;
    String jarFileName = null;
    String pathToJar = null;

    try {
      Class<CommandLineUtilities> cls = CommandLineUtilities.class;

      String url = cls.getResource(
          cls.getSimpleName() + ".class").toString();

      if (url.indexOf(".jar") >= 0) {
        int index = url.lastIndexOf(
            cls.getName().replace(".", "/") + ".class");
        jarBaseUrl = url.substring(0, index);

        index = jarBaseUrl.lastIndexOf("!");
        if (index >= 0) {
          url = url.substring(0, index);
          index = url.lastIndexOf("/");

          if (index >= 0) {
            jarFileName = url.substring(index + 1);
          }

          url = url.substring(0, index);
          index = url.indexOf("/");
          pathToJar = url.substring(index);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);

    } finally {
      JAR_BASE_URL = jarBaseUrl;
      JAR_FILE_NAME = jarFileName;
      PATH_TO_JAR = pathToJar;
    }
  }

  /**
   * Private default constructor.
   */
  private CommandLineUtilities() {
    // do nothing
  }

  /**
   * Utility method to ensure a command line argument with the specified index
   * exists and if not then throws an exception.
   *
   * @param args  The array of command line arguments.
   * @param index The index to check.
   * @throws IllegalArgumentException If the argument does not exist.
   */
  public static void ensureArgument(String[] args, int index) {
    if (index >= args.length) {
      String msg = "Missing expected argument following " + args[index - 1];

      System.err.println();
      System.err.println(msg);

      setLastLoggedAndThrow(new IllegalArgumentException(msg));
    }
  }

  /**
   * Returns a new {@link String} array that contains the same elements as
   * the specified array except for the first N arguments where N is the
   * specified count.
   *
   * @param args  The array of command line arguments.
   * @param count The number of arguments to shift.
   * @return The shifted argument array.
   * @throws IllegalArgumentException If the specified count is negative.
   */
  public static String[] shiftArguments(String[] args, int count) {
    if (count < 0) {
      throw new IllegalArgumentException(
          "The specified count cannot be negative: " + count);
    }
    String[] args2 = new String[args.length - count];
    for (int index = 0; index < args2.length; index++) {
      args2[index] = args[index + count];
    }
    return args2;
  }

  /**
   * Returns a descriptor for the source of the {@link CommandLineValue}
   * for building error messages.
   *
   * @param commandLineValue The {@link CommandLineValue}.
   */
  private static <T extends Enum<T> & CommandLineOption<T>> String
    sourceDescriptor(CommandLineValue<T> commandLineValue)
  {
    switch (commandLineValue.getSource()) {
      case COMMAND_LINE:
        return commandLineValue.getSpecifier() + " option";
      case ENVIRONMENT:
        return commandLineValue.getSpecifier() + " environment variable";
      default:
        return commandLineValue.getOption().getCommandLineFlag() + " default";
    }
  }

  /**
   * Stores the option and its value(s) in the specified option map, first
   * checking to ensure that the option is NOT already specified and
   * checking if the option has any conflicts.  If the specified option is
   * deprecated then a warning message is returned.  If only a single option
   * is specified then it is placed as the value.  If more than one option is
   * specified then the value in the map is an array of the specified values.
   *
   * @param optionMap The {@link Map} to put the option in.
   * @param value the {@link CommandLineValue} to add to the {@link Map}.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> void putValue(
      Map<T, CommandLineValue<T>> optionMap,
      CommandLineValue<T>         value)
  {
    T option = value.getOption();
    Set<T> optionKeys = optionMap.keySet();
    CommandLineValue<T> prevValue = optionMap.get(option);
    if (prevValue != null) {
      String msg = (prevValue.getSpecifier().equals(value.getSpecifier()))
          ? "The " + sourceDescriptor(value) + " cannot be specified more than "
            + "once."
          : "Cannot specify both the " + sourceDescriptor(prevValue)
            + " and " + sourceDescriptor(value) + ".";

      System.err.println();
      System.err.println(msg);

      setLastLoggedAndThrow(new IllegalArgumentException(msg));
    }

    // check for conflicts if the value is NOT a no-arg option with false value
    if (!checkNoArgFalse(value) && (value.getSource() != DEFAULT)) {
      Set<T> conflicts = option.getConflicts();
      for (T opt : optionKeys) {
        if (conflicts != null && conflicts.contains(opt)) {
          // get the command-line value
          CommandLineValue<T> conflictValue = optionMap.get(opt);

          // no conflict if the other is a defaulted value
          if (conflictValue.getSource() == DEFAULT) continue;

          // no conflict if the other is a no-arg option with a false value
          if (checkNoArgFalse(conflictValue)) continue;

          // handle the conflict
          String msg = "Cannot specify both the "
              + sourceDescriptor(conflictValue) + " and "
              + sourceDescriptor(value) + ".";

          System.err.println();
          System.err.println(msg);

          setLastLoggedAndThrow(new IllegalArgumentException(msg));
        }
      }
    }

    // check for deprecation
    if (option.isDeprecated()) {
      System.err.println();
      System.err.println("WARNING: The " + sourceDescriptor(value)
                             + " option is deprecated and will be removed in a "
                             + "future release.");
      Set<T> alternatives = option.getDeprecationAlternatives();
      if (alternatives.size() == 1) {
        T alternative = alternatives.iterator().next();
        System.err.println();
        System.err.println("Consider using " + alternative.getCommandLineFlag()
                               + " instead.");
      } else if (alternatives.size() > 1) {
        System.err.println();
        System.err.println("Consider using one of the following instead:");
        for (T alternative : alternatives) {
          System.err.println("     " + alternative.getCommandLineFlag());
        }
      }
      System.err.println();
    }

    // put it in the option map
    optionMap.put(option, value);
  }

  /**
   * Checks if the specified {@link CommandLineValue} is a zero or single
   */
  private static <T extends Enum<T> & CommandLineOption<T>> boolean
    checkNoArgFalse(CommandLineValue<T> cmdLineValue)
  {
    T option = cmdLineValue.getOption();
    // special-case no-arg boolean flags
    int minParamCount = option.getMinimumParameterCount();
    int maxParamCount = option.getMaximumParameterCount();
    return (minParamCount == 0 && maxParamCount >= 0
            && option.getDefaultParameters() != null
            && option.getDefaultParameters().size() == 1
            && "false".equalsIgnoreCase(option.getDefaultParameters().get(0))
            && cmdLineValue.getParameters() != null
            && cmdLineValue.getParameters().size() == 1
            && "false".equalsIgnoreCase(cmdLineValue.getParameters().get(0)));
  }

  /**
   * Returns the enumerated {@link CommandLineOption} value associated with
   * the specified command line flag and enumerated {@link CommandLineOption}
   * class.
   *
   * @param enumClass       The {@link Class} for the {@link CommandLineOption}
   *                        implementation.
   * @param commandLineFlag The command line flag.
   * @return The enumerated {@link CommandLineOption} or <tt>null</tt> if not
   * found.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> T lookup(
      Class<T> enumClass,
      String commandLineFlag) {
    // just iterate to find it rather than using a lookup map given that
    // enums are usually not more than a handful of values
    EnumSet<T> enumSet = EnumSet.allOf(enumClass);
    for (T enumVal : enumSet) {
      if (enumVal.getCommandLineFlag().equals(commandLineFlag)) {
        return enumVal;
      }
    }
    return null;
  }

  /**
   * Validates the specified {@link Set} of specified {@link CommandLineOption}
   * instances and ensures that they logically make sense together.  This
   * checks for the existing of at least one primary option (if primary options
   * exist), ensures there are no conflicts and that all dependencies are
   * satisfied.
   *
   * @throws IllegalArgumentException If the specified options are invalid
   *                                  together.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> void validateOptions(
      Class<T>                    enumClass,
      Map<T, CommandLineValue<T>> optionValues)
      throws IllegalArgumentException
  {
    // check if we need a primary option
    boolean primaryRequired = false;
    EnumSet<T> enumSet = EnumSet.allOf(enumClass);
    for (T enumVal : enumSet) {
      if (enumVal.isPrimary()) {
        primaryRequired = true;
        break;
      }
    }

    if (primaryRequired) {
      // if primary option is required then check for at least one
      int primaryCount = 0;
      for (T option : optionValues.keySet()) {
        if (option.isPrimary()) {
          primaryCount++;
        }
      }
      if (primaryCount == 0) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("Must specify at least one of the following:");
        for (T option : enumSet) {
          if (option.isPrimary()) {
            pw.println("     " + option.getCommandLineFlag()
                           + (option.isDeprecated() ? " (deprecated)" : ""));
          }
        }

        String msg = sw.toString();
        pw.flush();
        System.err.println();
        System.err.println(msg);
        setLastLoggedAndThrow(new IllegalArgumentException(msg));
      }
    }

    // check for conflicts and dependencies
    for (CommandLineValue<T> cmdLineValue : optionValues.values()) {
      if (cmdLineValue.getSource() == DEFAULT) continue;
      T option = cmdLineValue.getOption();
      Set<T> conflicts = option.getConflicts();
      Set<Set<T>> dependencies = option.getDependencies();
      if (conflicts != null) {
        for (T conflict : conflicts) {
          // skip if the same option -- cannot conflict with itself
          if (option == conflict) continue;

          // check if the conflict is present
          CommandLineValue<T> conflictValue = optionValues.get(conflict);
          if (conflictValue != null) {
            if (conflictValue.getSource() == DEFAULT) continue;
            String msg = "Cannot specify both the "
                + sourceDescriptor(cmdLineValue)
                + " and the " + sourceDescriptor(conflictValue);
            System.err.println();
            System.err.println(msg);

            setLastLoggedAndThrow(new IllegalArgumentException(msg));
          }
        }
      }
      boolean satisfied = (dependencies == null || dependencies.size() == 0);
      if (!satisfied) {
        for (Set<T> dependencySet : dependencies) {
          if (optionValues.keySet().containsAll(dependencySet)) {
            satisfied = true;
            break;
          }
        }
      }
      if (!satisfied) {
        System.err.println();
        if (dependencies.size() == 1) {
          System.err.println(
              "The " + sourceDescriptor(cmdLineValue) + " also requires:");
          Set<T> dependencySet = dependencies.iterator().next();
          for (T dependency : dependencySet) {
            if (!optionValues.containsKey(dependency)) {
              System.err.println("     " + dependency.getCommandLineFlag());
            }
          }
        } else {
          System.err.println(
              "The " + sourceDescriptor(cmdLineValue) + " also requires:");
          String leader = "     ";
          for (Set<T> dependencySet : dependencies) {
            String prefix = "";
            String prevOption = null;
            System.err.print(leader);
            leader = "  or ";
            for (T dependency : dependencySet) {
              int count = 0;
              if (!optionValues.containsKey(dependency)) {
                if (prevOption != null) {
                  count++;
                  System.err.print(prefix + prevOption);
                }
                prevOption = dependency.getCommandLineFlag();
                prefix = ", ";
              }
              if (count > 0) {
                System.err.print(" and ");
              }
              System.err.println(prevOption);
            }
          }
        }
        setLastLoggedAndThrow(new IllegalArgumentException(
            "Missing dependencies for " + sourceDescriptor(cmdLineValue)));
      }
    }
  }

  /**
   * Creates a lookup map of {@link String} command-line flags to the
   * {@link CommandLineOption} values that they map to.  This includes any
   * synonyms for the options if they exist.
   *
   * @param enumClass The enumerated class for the {@link CommandLineOption}.
   */
  private static <T extends Enum<T> & CommandLineOption<T>> Map<String, T>
    createFlagLookupMap(Class<T> enumClass)
  {
    // get all the options
    EnumSet<T> enumSet = EnumSet.allOf(enumClass);

    // create a lookup map for the flags
    Map<String, T> lookupMap = new LinkedHashMap<>();
    for (T option : enumSet) {
      // get the flag
      String flag = option.getCommandLineFlag();

      // do a sanity check
      if (lookupMap.containsKey(flag)) {
        throw new IllegalStateException(
            "Command-line flag (" + flag + ") cannot resolve to different "
                + "options (" + lookupMap.get(flag) + " and " + option
                + ").  It must be unique.");
      }

      // add the primary flag to the lookup map
      lookupMap.put(flag, option);

      // add the synonym flags if any
      Set<String> synonymFlags = option.getSynonymFlags();
      if (synonymFlags == null) synonymFlags = Collections.emptySet();
      for (String synonym : synonymFlags) {
        // check the synonym against the primary flag (sanity check)
        if (synonym.equals(flag)) {
          throw new IllegalStateException(
              "Synonym command-line (" + flag + ") for option (" + option
                  + ") is the same as the primary flag.");
        }

        // do a sanity check
        if (lookupMap.containsKey(synonym)) {
          throw new IllegalStateException(
              "Command-line flag (" + flag + ") cannot resolve to different "
                  + "options (" + lookupMap.get(flag) + " and " + option
                  + ").  It must be unique.");

        }

        // add to the lookup map
        lookupMap.put(synonym, option);
      }
    }

    // return the lookup map
    return lookupMap;
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.  This function will attempt to find values for command-line
   * options that are not explicitly specified on the command line by using
   * environment variables for those options have environment variable
   * equivalents defined.
   *
   * @param enumClass The enumerated {@link CommandLineOption} class.
   * @param args The arguments to parse.
   * @param processor The {@link ParameterProcessor} to use for handling the
   *                  parameters to the options.
   * @return A {@link Map} of {@link CommandLineOption} keys to
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, CommandLineValue<T>>
  parseCommandLine(Class<T>               enumClass,
                   String[]               args,
                   ParameterProcessor<T>  processor)
  {
    return parseCommandLine(
        enumClass, args, processor, false, null);
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.  Depending on the specified value for <tt>ignoreEnvironment<tt>
   * this function will optionally attempt to find values for command-line
   * options that are not explicitly specified on the command line by using
   * environment variables for those options have environment variable
   * equivalents defined.
   *
   * @param enumClass The enumerated {@link CommandLineOption} class.
   * @param args The arguments to parse.
   * @param processor The {@link ParameterProcessor} to use for handling the
   *                  parameters to the options.
   * @param ignoreEnvironment Flag indicating if the environment variables
   *                          should be ignored in the processing.
   * @return A {@link Map} of {@link CommandLineOption} keys to
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, CommandLineValue<T>>
  parseCommandLine(Class<T>               enumClass,
                   String[]               args,
                   ParameterProcessor<T>  processor,
                   boolean                ignoreEnvironment)
  {
    return parseCommandLine(
        enumClass, args, processor, false, null);
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.  This function will optionally attempt to find values for
   * command-line options that are not explicitly specified on the command line
   * by using environment variables for those options have environment variable
   * equivalents defined.  Use of the environment is disabled if the
   * <tt>ignoreEnvOption</tt> parameter is non-null and that option is present
   * in the explicitly specified command-line arguments and either has no
   * parameter value or any value other than <tt>false</tt>.
   *
   * @param enumClass The enumerated {@link CommandLineOption} class.
   * @param args The arguments to parse.
   * @param processor The {@link ParameterProcessor} to use for handling the
   *                  parameters to the options.
   * @param ignoreEnvOption The optional command-line option value that if
   *                        present with no value or present with any value
   *                        other than <tt>false</tt> will cause the environment
   *                        to be ignored in processing.
   *
   * @return A {@link Map} of {@link CommandLineOption} keys to
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, CommandLineValue<T>>
  parseCommandLine(Class<T>               enumClass,
                   String[]               args,
                   ParameterProcessor<T>  processor,
                   T                      ignoreEnvOption)
  {
    return parseCommandLine(
        enumClass, args, processor, false, ignoreEnvOption);
  }

  /**
   * Handles processing the specified parameters for the specified option.
   *
   * @param option The relevant option.
   * @param processor The {@link ParameterProcessor}, or <tt>null</tt> if none.
   * @param params The {@link List} of {@link String} parameters.
   * @return The processed value.
   */
  private static <T extends Enum<T> & CommandLineOption<T>> Object processValue(
      T                     option,
      ParameterProcessor<T> processor,
      List<String>          params)
  {
    // process the parameters
    if (processor != null) {
      // handle the case where a processor is defined for the parameters
      Object value = null;
      try {
        value = processor.process(option, params);
      } catch (Exception e) {
        System.err.println();
        System.err.println("Bad parameters for " + option.getCommandLineFlag()
                               + " option:");
        for (String p : params) {
          System.err.println("    o " + p);
        }
        System.err.println();
        System.err.println(e.getMessage());
        if (e instanceof RuntimeException) {
          LoggingUtilities.setLastLoggedAndThrow((RuntimeException) e);
        }
        LoggingUtilities.setLastLoggedAndThrow(new RuntimeException(e));
      }
      return value;

    } else if (params.size() == 0) {
      // handle the case of no parameters and no parameter processor
      return null;

    } else if (params.size() == 1) {
      // handle the case of one parameter and no parameter processor
      return params.get(0);

    } else {
      // handle the case of multiple parameters and no parameter processor
      return params.toArray();
    }
  }

  /**
   * Parses the command line arguments and returns a {@link Map} of those
   * arguments.
   *
   * @param enumClass The enumerated {@link CommandLineOption} class.
   * @param args The arguments to parse.
   * @param processor The {@link ParameterProcessor} to use for handling the
   *                  parameters to the options.
   * @param ignoreEnvironment Flag indicating if the environment variables
   *                          should be ignored in the processing.
   * @param ignoreEnvOption The option to trigger ignoring the environment.
   *
   * @return A {@link Map} of {@link CommandLineOption} keys to
   */
  private static <T extends Enum<T> & CommandLineOption<T>> Map<T, CommandLineValue<T>>
    parseCommandLine(Class<T>               enumClass,
                     String[]               args,
                     ParameterProcessor<T>  processor,
                     boolean                ignoreEnvironment,
                     T                      ignoreEnvOption)
  {
    // create a lookup map for the flags to their options
    Map<String, T> lookupMap = createFlagLookupMap(enumClass);

    // iterate over the args and build a map
    Map<T, CommandLineValue<T>> result     = new LinkedHashMap<>();
    Map<T, String>              usedFlags  = new LinkedHashMap<>();
    for (int index = 0; index < args.length; index++) {
      // get the next flag
      String flag = args[index];

      // determine the option from the flag
      T option = lookupMap.get(flag);

      // check if the option is recognized
      if (option == null) {
        System.err.println();
        System.err.println("Unrecognized option: " + flag);

        setLastLoggedAndThrow(new IllegalArgumentException(
            "Unrecognized command line option: " + flag));
      }

      // check if the option has already been specified
      String usedFlag = usedFlags.get(option);
      if (usedFlag != null && usedFlag.equals(flag)) {
        System.err.println();
        System.err.println("Option specified more than once: " + flag);

        setLastLoggedAndThrow(new IllegalArgumentException(
            "Option specified more than once: " + flag));

      } else if (usedFlag != null) {
        System.err.println();
        System.err.println(
            "Option specified more than once: " + usedFlag + " and " + flag);

        setLastLoggedAndThrow(new IllegalArgumentException(
            "Option specified more than once: " + usedFlag + " and " + flag));
      }

      // get the option parameters
      int minParamCount = option.getMinimumParameterCount();
      int maxParamCount = option.getMaximumParameterCount();
      if (maxParamCount > 0 && maxParamCount < minParamCount) {
        throw new IllegalStateException(
            "The non-negative maximum parameter count is less than the minimum "
            + "parameter count.  min=[ " + minParamCount + " ], max=[ "
            + maxParamCount + " ], option=[ " + option + " ]");
      }
      List<String> params = new ArrayList<>(maxParamCount<0 ? 5:maxParamCount);
      if (minParamCount > 0) {
        // check if there are enough parameters
        int max = index + minParamCount;
        for (index++; index <= max; index++) {
          ensureArgument(args, index);
          params.add(args[index]);
        }
        index--;
      }

      // check if we have a zero-argument parameter
      if (minParamCount == 0 && maxParamCount == 0
          && option.getDefaultParameters() != null
          && option.getDefaultParameters().size() == 1
          && "false".equalsIgnoreCase(option.getDefaultParameters().get(0)))
      {
        // allow a zero-argument parameter to be followed by "true" or "false"
        if (args.length > (index + 1)) {
          // we have arguments after this one -- check the next one
          String param = args[index+1].trim().toLowerCase();

          // check if it is a parameter or a command-line flag
          if (!param.startsWith("-")) {
            // looks like a parameter since a flag starts with "-"
            switch (param) {
              case "true":
              case "false":
                params.add(args[++index].trim().toLowerCase());
                break;
              default:
                throw new IllegalArgumentException(
                    "The " + flag + " command line option can be specified "
                    + "with no parameters, but if a parameter is provided it "
                    + "must be \"true\" or \"false\": " + args[index + 1]);
            }
          }
        }

      } else {
        // get the parameters from the command line
        int bound = (maxParamCount < 0)
            ? args.length
            : index + (maxParamCount - minParamCount) + 1;
        if (bound > args.length) bound = args.length;
        for (int nextIndex = index + 1;
             nextIndex < bound && !lookupMap.containsKey(args[nextIndex]);
             nextIndex++)
        {
          params.add(args[nextIndex]);
          index++;
        }
      }

      // check if we have a zero-parameter option with a "false" default
      if (minParamCount == 0 && maxParamCount == 0 && params.size() == 0
          && option.getDefaultParameters() != null
          && option.getDefaultParameters().size() == 1
          && "false".equalsIgnoreCase(option.getDefaultParameters().get(0)))
      {
        params.add("true");
      }

      // process the parameters
      Object processedValue = processValue(option, processor, params);

      // create the command-line value
      CommandLineValue<T> cmdLineVal = new CommandLineValue<>(option,
                                                              COMMAND_LINE,
                                                              flag,
                                                              processedValue,
                                                              params);

      // add to the options map
      putValue(result, cmdLineVal);

    }

    // optionally process the environment
    ignoreEnvironment
        = (ignoreEnvironment
          || (result.containsKey(ignoreEnvOption)
              && (!Boolean.FALSE.equals(result.get(ignoreEnvOption)))));
    if (!ignoreEnvironment) {
      try {
        processEnvironment(enumClass, processor, result);
      } catch (NullPointerException e) {
        e.printStackTrace();
        throw e;
      }
    }

    try {
      // handle setting the default values
      processDefaults(enumClass, processor, result);
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    }

    // validate the options
    try {
      validateOptions(enumClass, result);
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    }

    return result;
  }

  /**
   * Checka for command-line option values in the environment.
   *
   */
  private static <T extends Enum<T> & CommandLineOption<T>> void
  processEnvironment(Class<T>                     enumClass,
                     ParameterProcessor<T>        processor,
                     Map<T, CommandLineValue<T>>  optionValues)
  {
    processEnvironment(EnumSet.allOf(enumClass),
                       processor,
                       optionValues,
                       false);

    Set<T> optionKeys       = optionValues.keySet();
    Set<T> fallbackOptions  = new LinkedHashSet<>();
    for (T option : optionKeys) {
      // get the dependency sets
      Set<Set<T>> dependencySets = option.getDependencies();

      // check if no dependencies
      if (dependencySets == null) continue;
      if (dependencySets.size() == 0) continue;

      // set the flag indicating the dependencies are not satisfied
      boolean satisfied = false;

      // iterate over the dependency sets and see if at least one is satisfied
      for (Set<T> dependencySet: dependencySets) {
        if (optionValues.keySet().containsAll(dependencySet)) {
          satisfied = true;
          break;
        }
      }

      // check if the option is not satisfied
      if (!satisfied) {
        // find the first dependency set with missing items with fallbacks
        for (Set<T> dependencySet: dependencySets) {
          // set the fallback count to zero and create the missing set
          int     fallbackCount = 0;
          Set<T>  missingSet    = new LinkedHashSet<>();

          // iterate over the options
          for (T dependency: dependencySet) {
            // check if dependency is missing
            if (optionValues.containsKey(dependency)) continue;

            // add to the missing set
            missingSet.add(dependency);

            // check if the missing item has fallbacks
            List<String> fallbacks = dependency.getEnvironmentFallbacks();
            if (fallbacks != null && fallbacks.size() > 0) fallbackCount++;
          }

          // check if the fallback count and missing count are equal and if
          // so then use this dependency set
          if (missingSet.size() > 0 && missingSet.size() == fallbackCount) {
            fallbackOptions.addAll(missingSet);
            break;
          }
        }
      }
    }

    // check the fallback options
    if (fallbackOptions.size() > 0) {
      System.err.println();
      processEnvironment(fallbackOptions, processor, optionValues, true);
    }
  }
  /**
   * Checka for command-line option values in the environment.
   *
   */
  private static <T extends Enum<T> & CommandLineOption<T>> void
    processEnvironment(Set<T>                       enumSet,
                       ParameterProcessor<T>        processor,
                       Map<T, CommandLineValue<T>>  optionValues,
                       boolean                      fallBacks)
  {
    // get the environment
    Map<String, String> env = System.getenv();

    // iterate over the options
    for (T option: enumSet) {
      // prefer explicit command-line arguments, so skip if already present
      if (optionValues.containsKey(option)) continue;

      List<String> envVars = null;
      if (fallBacks) {
        // get the fallbacks
        envVars = option.getEnvironmentFallbacks();
        if (envVars == null) envVars = Collections.emptyList();

      } else {
        // get the environment variable and its synonyms
        String      envVar      = option.getEnvironmentVariable();
        Set<String> synonymSet  = option.getEnvironmentSynonyms();
        envVars = new ArrayList<>(synonymSet==null ? 1 : synonymSet.size() + 1);
        if (envVar != null) envVars.add(envVar);
        if (synonymSet != null) envVars.addAll(synonymSet);
      }

      // iterate over the items in the list
      String envVal   = null;
      String foundVar = null;
      for (String envVar: envVars) {
        // check if null
        if (envVar == null) continue;

        // check if contained in the environment
        envVal = env.get(envVar);
        if (envVal != null) {
          foundVar = envVar;
          break;
        }
      }

      // check if no value
      if (envVal == null) continue;

      // process the env value
      int minParamCount = option.getMinimumParameterCount();
      int maxParamCount = option.getMaximumParameterCount();
      if (maxParamCount > 0 && maxParamCount < minParamCount) {
        throw new IllegalStateException(
            "The non-negative maximum parameter count is less than the minimum "
                + "parameter count.  min=[ " + minParamCount + " ], max=[ "
                + maxParamCount + " ], option=[ " + option + " ]");
      }
      List<String> params = null;

      // check the number of parameters
      if (minParamCount == 0 && maxParamCount == 0) {
        // no parameters, these are boolean on/off options
        switch (envVal.trim().toLowerCase()) {
          case "":
          case "true":
            params = List.of("true");
            break;
          case "false":
            params = List.of("false");
            break;
          default:
            throw new IllegalArgumentException(
                "The specified value for the " + foundVar
                + " environment variable can only be \"true\""
                + " or \"false\": " + foundVar);
        }

      } else if (minParamCount <= 1 && maxParamCount == 1) {
        // handle the single parameter case
        params = List.of(envVal);

      } else if ((maxParamCount > 1 || maxParamCount < 0)
                 && (JSON_ARRAY_PATTERN.matcher(envVal).matches()))
      {
        // handle the case of multiple parameters and a JSON array
        JsonArray jsonArray = JsonUtils.parseJsonArray(envVal.trim());
        params = new ArrayList<>(jsonArray.size());
        for (JsonString jsonString: jsonArray.getValuesAs(JsonString.class)) {
          params.add(jsonString.getString());
        }

      } else if (maxParamCount > 1 || maxParamCount < 0) {
        // handle the case of multiple parameters, splitting on commas & spaces
        String[] tokens = envVal.split("(\\s*,\\s*|\\s+)");
        params = Arrays.asList(tokens);
      }

      // get the parameter count
      int paramCount = params.size();

      // check the parameter count
      if (paramCount != 1 || minParamCount != 0 || maxParamCount != 0) {
        // handle the options with parameters
        if (paramCount < minParamCount) {
          throw new IllegalArgumentException(
              "Expected at least " + minParamCount + " parameter(s) but "
                  + "found " + paramCount + " for " + foundVar
                  + " environment variable: " + envVal);
        }
        if ((maxParamCount >= 0) && (paramCount > maxParamCount)) {
          throw new IllegalArgumentException(
              "Expected at most " + maxParamCount + " parameter(s) but "
                  + "found " + paramCount + " for " + foundVar
                  + " environment variable: " + envVal);
        }
      }

      // process the parameters
      Object processedValue = processValue(option, processor, params);

      // create the command line value
      CommandLineValue<T> cmdLineVal = new CommandLineValue<>(option,
                                                              ENVIRONMENT,
                                                              foundVar,
                                                              processedValue,
                                                              params);

      // put the value
      putValue(optionValues, cmdLineVal);
    }
  }

  /**
   * Checka for command-line option values in the environment.
   *
   */
  private static <T extends Enum<T> & CommandLineOption<T>> void
    processDefaults(Class<T>                    enumClass,
                    ParameterProcessor<T>       processor,
                    Map<T, CommandLineValue<T>> optionValues)
  {
    // get all the options
    EnumSet<T> enumSet = EnumSet.allOf(enumClass);

    // iterate over the options
    for (T option: enumSet) {
      // prefer explicit command-line arguments, so skip if already present
      if (optionValues.containsKey(option)) continue;

      // get the default parameters
      List<String> params = option.getDefaultParameters();

      // check if there are none
      if (params == null || params.size() == 0) continue;

      // process the parameters
      Object processedValue = processValue(option, processor, params);

      // create the command line value
      CommandLineValue<T> cmdLineVal = new CommandLineValue<>(option,
                                                              DEFAULT,
                                                              processedValue,
                                                              params);

      // put the value
      putValue(optionValues, cmdLineVal);
    }
  }

  /**
   * Processes the {@link Map} of {@link CommandLineOption} keys to {@link
   * CommandLineValue} values and produces the {@link Map} of {@link
   * CommandLineOption} keys to {@linkplain CommandLineValue#getProcessedValue()
   * processed values}.
   *
   * @param optionValues The {@link Map} of option keys to {@link
   *                     CommandLineValue} values.
   * @param processedValues The {@link Map} to populate with the processed
   *                        values if specified, if <tt>null</tt> a new {@link
   *                        Map} is created and returned.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, Object>
    processCommandLine(Map<T, CommandLineValue<T>>  optionValues,
                       Map<T, Object>               processedValues)
  {
    return processCommandLine(optionValues,
                              processedValues,
                              null,
                              null);
  }

  /**
   * Processes the {@link Map} of {@link CommandLineOption} keys to {@link
   * CommandLineValue} values and produces the {@link Map} of {@link
   * CommandLineOption} keys to {@linkplain CommandLineValue#getProcessedValue()
   * processed values}.
   *
   * @param optionValues The {@link Map} of option keys to {@link
   *                     CommandLineValue} values.
   * @param processedValues The {@link Map} to populate with the processed
   *                        values if specified, if <tt>null</tt> a new {@link
   *                        Map} is created and returned.
   * @param jsonBuilder If not <tt>null</tt> then this {@link JsonObjectBuilder}
   *                    is populated with startup option information.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, Object>
  processCommandLine(Map<T, CommandLineValue<T>>  optionValues,
                     Map<T, Object>               processedValues,
                     JsonObjectBuilder            jsonBuilder)
  {
    return processCommandLine(optionValues,
                              processedValues,
                              jsonBuilder,
                              null);

  }

  /**
   * Processes the {@link Map} of {@link CommandLineOption} keys to {@link
   * CommandLineValue} values and produces the {@link Map} of {@link
   * CommandLineOption} keys to {@linkplain CommandLineValue#getProcessedValue()
   * processed values}.
   *
   * @param optionValues The {@link Map} of option keys to {@link
   *                     CommandLineValue} values.
   * @param processedValues The {@link Map} to populate with the processed
   *                        values if specified, if <tt>null</tt> a new {@link
   *                        Map} is created and returned.
   * @param stringBuilder If not <tt>null</tt>then this {@link StringBuilder}
   *                      is populated with JSON text describing the startup
   *                      options.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, Object>
  processCommandLine(Map<T, CommandLineValue<T>>  optionValues,
                     Map<T, Object>               processedValues,
                     StringBuilder                stringBuilder)
  {
    return processCommandLine(optionValues,
                              processedValues,
                              null,
                              stringBuilder);
  }

  /**
   * Processes the {@link Map} of {@link CommandLineOption} keys to {@link
   * CommandLineValue} values and produces the {@link Map} of {@link
   * CommandLineOption} keys to {@linkplain CommandLineValue#getProcessedValue()
   * processed values}.
   *
   * @param optionValues The {@link Map} of option keys to {@link
   *                     CommandLineValue} values.
   * @param processedValues The {@link Map} to populate with the processed
   *                        values if specified, if <tt>null</tt> a new {@link
   *                        Map} is created and returned.
   * @param jsonBuilder If not <tt>null</tt> then this {@link JsonObjectBuilder}
   *                    is populated with startup option information.
   * @param stringBuilder If not <tt>null</tt>then this {@link StringBuilder}
   *                      is populated with JSON text describing the startup
   *                      options.
   */
  public static <T extends Enum<T> & CommandLineOption<T>> Map<T, Object>
    processCommandLine(Map<T, CommandLineValue<T>>  optionValues,
                       Map<T, Object>               processedValues,
                       JsonObjectBuilder            jsonBuilder,
                       StringBuilder                stringBuilder)
  {
    // create the result map if not specified
    Map<T, Object> result = (processedValues == null) ? new LinkedHashMap<>()
        : processedValues;

    // check if we are generating the JSON
    boolean doJson = (jsonBuilder != null || stringBuilder != null);

    // check if we need to create the JSON object builder
    JsonObjectBuilder job = (doJson && jsonBuilder == null)
        ? Json.createObjectBuilder() : jsonBuilder;

    // iterate over the option values and handle them
    optionValues.forEach( (key, cmdLineVal) -> {
      // create the sub-builder if doing JSON
      JsonObjectBuilder job2 = (doJson) ? Json.createObjectBuilder() : null;

      // get the parameters
      List<String> params = cmdLineVal.getParameters();

      // add the parameters if doing JSON
      if (doJson) {
        // check if there is only a single parameter
        if (params.size() == 1) {
          // handle a single parameter
          job2.add("value", params.get(0));
        } else {
          // handle multiple parameters
          JsonArrayBuilder jab = Json.createArrayBuilder();
          for (String param : params) {
            jab.add(param);
          }
          job2.add("values", jab);
        }

        // add the source
        job2.add("source", cmdLineVal.getSource().toString());

        // check if we have a specifier to add
        if (cmdLineVal.getSpecifier() != null) {
          // add the specifier
          job2.add("via", cmdLineVal.getSpecifier());
        }

        // add to the JsonObjectBuilder
        job.add(key.toString(), job2);
      }

      // add to the map
      result.put(key, cmdLineVal.getProcessedValue());
    });

    // append the JSON text if requested
    if (stringBuilder != null) {
      stringBuilder.append(JsonUtils.toJsonText(job));
    }

    // return the map
    return result;
  }

  /**
   * Checks if the specified {@link Class} was the class whose static
   * <tt>main(String[])</tt> function was called to begin execution of the
   * current process.
   *
   * @param cls The {@link Class} to test for.
   *
   * @return <tt>true</tt> if the specified class' static
   *         <tt>main(String[])</tt> function was called to begin execution of
   *         the current process.
   */
  public static boolean checkClassIsMain(Class cls) {
    // check if called from the ConfigurationManager.main() directly
    Throwable t = new Throwable();
    StackTraceElement[] trace = t.getStackTrace();
    StackTraceElement lastStackFrame = trace[trace.length-1];
    String className = lastStackFrame.getClassName();
    String methodName = lastStackFrame.getMethodName();
    return ("main".equals(methodName) && cls.getName().equals(className));
  }

  /**
   * Returns a multi-line bulleted list of the specified options with the
   * specified indentation.
   *
   * @param indent The number of spaces to indent.
   * @param options The zero or more options to write.
   *
   * @return The multi-line bulleted list of options.
   */
  public static String formatUsageOptionsList(int                  indent,
                                              CommandLineOption... options)
  {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter(sw);
    int maxLength = 0;
    StringBuilder sb = new StringBuilder();
    for (int index = 0; index < indent; index++) {
      sb.append(" ");
    }
    String indenter = sb.toString();

    for (CommandLineOption option : options) {
      if (option.getCommandLineFlag().length() > maxLength) {
        maxLength = option.getCommandLineFlag().length();
      }
    }
    String spacer = "  ";
    String bullet = "o ";
    maxLength += (bullet.length() + spacer.length());
    // check how many options per line we can fit
    int columnCount = (80 - indent - spacer.length()) / maxLength;

    // check if we can balance things out if we have a single dangling option
    if (options.length == columnCount + 1) {
        columnCount--;
    }

    // if less than 1, then set a minimum of 1 column
    if (columnCount < 1) columnCount = 1;

    int columnIndex = 0;
    for (CommandLineOption option: options) {
      String flag       = option.getCommandLineFlag();
      int    spaceCount = maxLength - flag.length() - bullet.length();
      if (columnIndex == 0) pw.print(indenter);
      else pw.print(spacer);
      pw.print(bullet);
      pw.print(flag);
      for (int index = 0; index < spaceCount; index++) {
        pw.print(" ");
      }
      if (columnIndex == columnCount - 1) {
        pw.println();
        columnIndex = 0;
      } else {
        columnIndex++;
      }
    }
    if (columnIndex != 0) {
      pw.println();
    }
    pw.flush();
    return sw.toString();
  }
}
