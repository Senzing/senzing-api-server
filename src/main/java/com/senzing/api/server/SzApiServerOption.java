package com.senzing.api.server;

import com.senzing.cmdline.CommandLineOption;

import java.util.*;

import static com.senzing.util.CollectionUtilities.recursivelyUnmodifiableMap;
import static com.senzing.api.server.mq.KafkaEndpoint.*;
import static com.senzing.api.server.mq.SqsEndpoint.*;
import static com.senzing.api.server.mq.RabbitEndpoint.*;
import static com.senzing.api.server.SzApiServerConstants.*;

/**
 * Describes the command-line options for {@link SzApiServer}.
 */
public enum SzApiServerOption implements CommandLineOption<SzApiServerOption>
{
  /**
   * <p>
   * Option for displaying help/usage for the API Server.  This option can
   * only be provided by itself and has no parameters.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--help</tt></li>
   *   <li>Command Line: <tt>-help</tt></li>
   * </ul>
   * </p>
   */
  HELP("--help", Set.of("-help"), true, 0),

  /**
   * <p>
   * Option for displaying the version number of the API Server.  This option
   * can only be provided by itself and has no parameters.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--version</tt></li>
   *   <li>Command Line: <tt>-version</tt></li>
   * </ul>
   * </p>
   */
  VERSION("--version", Set.of("-version"), true, 0),

  /**
   * <p>
   * Option for specifying the HTTP port for the API Server.  This has a single
   * parameter which can be a positive integer port number or can be zero (0)
   * to indicate binding to a randomly selected port number.  If not provided
   * then {@link SzApiServerConstants#DEFAULT_PORT_PARAM} is used.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--http-port {positive-port-number|0}</tt></li>
   *   <li>Command Line: <tt>-httpPort {positive-port-number|0}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_PORT="{positive-port-number|0}"</tt></tt></li>
   * </ul>
   * </p>
   */
  HTTP_PORT("--http-port", Set.of("-httpPort"),
            ENV_PREFIX + "PORT", null,
            1, DEFAULT_PORT_PARAM),

  /**
   * <p>
   * Option for specifying the bind address for the API Server.  The possible
   * values can be an actual network interface name, an IP address, the word
   * <tt>"loopback"</tt> for the loopback local address or <tt>"all"</tt> to
   * indicate all configured network interfaces.  The default value is
   * {@link SzApiServerConstants#DEFAULT_BIND_ADDRESS}.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--bind-addr {ip-address|loopback|all}</tt></li>
   *   <li>Command Line: <tt>-bindAddr {ip-address|loopback|all}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_BIND_ADDR="{ip-address|loopback|all}"</tt></tt></li>
   * </ul>
   * </p>
   */
  BIND_ADDRESS("--bind-addr",
               Set.of("-bindAddr"),
               ENV_PREFIX + "BIND_ADDR", null,
               1, DEFAULT_BIND_ADDRESS),

  /**
   * <p>
   * Option for specifying the base path (and optional alias base paths) for the
   * API Server.  The possible values should begin with a forward slash, but if
   * not it will be added.  To specify one or more aliases for the base path
   * use a comma-separated list of base paths.  The default value is
   * <tt>"/"</tt>.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--url-base-path {base-path}</tt></li>
   *   <li>Command Line: <tt>-urlBasePath {base-path}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_URL_BASE_PATH="{base-path}"</tt></tt></li>
   * </ul>
   * </p>
   */
  URL_BASE_PATH("--url-base-path",
            Set.of("-urlBasePath"),
            ENV_PREFIX + "URL_BASE_PATH",
            null, 1, "/"),

  /**
   * <p>
   * Option for specifying the module name to initialize the Senzing API's
   * with.  The default value is {@link
   * SzApiServerConstants#DEFAULT_MODULE_NAME}.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--module-name {module-name}</tt></li>
   *   <li>Command Line: <tt>-moduleName {module-name}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_MODULE_NAME="{module-name}"</tt></tt></li>
   * </ul>
   * </p>
   */
  MODULE_NAME("--module-name", Set.of("-moduleName"),
              ENV_PREFIX + "MODULE_NAME", null,
              1, DEFAULT_MODULE_NAME),

  /**
   * <p>
   * Option for specifying the INI file to initialize the Senzing API's with.
   * The parameter to this option should be a file path to an INI file.
   * Alternatively, one can specify {@link #INIT_FILE}, {@link #INIT_JSON} or
   * {@link #INIT_ENV_VAR}.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--ini-file {file-path}</tt></li>
   *   <li>Command Line: <tt>-iniFile {file-path}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_INI_FILE="{file-path}"</tt></tt></li>
   * </ul>
   * </p>
   */
  INI_FILE("--ini-file", Set.of("-iniFile"),
           ENV_PREFIX + "INI_FILE", null,
           true, 1),

  /**
   * <p>
   * Option for specifying the JSON init file to initialize the Senzing API's
   * with.  The parameter to this option should be a file path to a JSON init
   * file.  Alternatively, one can specify {@link #INI_FILE},
   * {@link #INIT_JSON} or {@link #INIT_ENV_VAR}.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--init-file {file-path}</tt></li>
   *   <li>Command Line: <tt>-initFile {file-path}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_INIT_FILE="{file-path}"</tt></tt></li>
   * </ul>
   * </p>
   */
  INIT_FILE("--init-file", Set.of("-initFile"),
            ENV_PREFIX + "INIT_FILE", null,
            true, 1),

  /**
   * <p>
   * Option for specifying the JSON text to initialize the Senzing API's
   * with.  The parameter to this option should be the actual JSON text with
   * which to initialize.  Alternatively, one can specify {@link #INI_FILE},
   * {@link #INIT_FILE} or {@link #INIT_ENV_VAR}.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--init-json {json-text}</tt></li>
   *   <li>Command Line: <tt>-initJson {json-text}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_INIT_JSON="{json-text}"</tt></tt></li>
   * </ul>
   * </p>
   */
  INIT_JSON("--init-json", Set.of("-initJson"),
            ENV_PREFIX + "INIT_JSON", null,
            true, 1),

  /**
   * <p>
   * Option for specifying the name of an environment variable which contains
   * the JSON text to initialize the Senzing API's with.  The parameter to this
   * option should be the name of the environment variable which contians the
   * actual JSON text with which to initialize.  Alternatively, one can specify
   * {@link #INI_FILE}, {@link #INIT_FILE} or {@link #INIT_JSON}.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--init-env-var {env-var-name}</tt></li>
   *   <li>Command Line: <tt>-initEnvVar {env-var-name}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_INIT_ENV_VAR="{env-var-name}"</tt></tt></li>
   * </ul>
   * </p>
   */
  INIT_ENV_VAR("--init-env-var", Set.of("-initEnvVar"),
               ENV_PREFIX + "INIT_ENV_VAR", null,
               true, 1),

  /**
   * <p>
   * This option is used with {@link #INI_FILE}, {@link #INIT_FILE}, {@link
   * #INIT_JSON} or {@link #INIT_ENV_VAR} to force a specific configuration ID
   * to be used for initialization and prevent automatic reinitialization to
   * pickup the latest default config ID.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--config-id {config-id}</tt></li>
   *   <li>Command Line: <tt>-configId {config-id}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_CONFIG_ID="{config-id}"</tt></tt></li>
   * </ul>
   * </p>
   */
  CONFIG_ID("--config-id", Set.of("-configId"),
            ENV_PREFIX + "CONFIG_ID", null, 1),

  /**
   * <p>
   * This option's absence leaves the API server in read/write mode, but its
   * presence activates read-only mode.  Read-only mode disables functions that
   * would modify the entity repository data, causing those functions to return
   * a 403 Forbidden response.  NOTE: this option will not only disable loading
   * data to the entity repository, but will also disable modifications to the
   * configuration even if the {@link #ENABLE_ADMIN} option is provided.  A
   * single parameter may optionally be specified as <tt>true</tt> or
   * <tt>false</tt> with <tt>false</tt> simulating the absence of the option.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--read-only [true|false]</tt></li>
   *   <li>Command Line: <tt>-readOnly [true|false]</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_READ_ONLY="{true|false}"</tt></tt></li>
   * </ul>
   * </p>
   */
  READ_ONLY("--read-only", Set.of("-readOnly"),
            ENV_PREFIX + "READ_ONLY", null,
            0, "false"),

  /**
   * <p>
   * This option's absence leaves the API server in standard mode, but its
   * presence enables administrative mode.  Administrative mode enables
   * administrative functions such as those that would modify the configuration.
   * If not specified then administrative functions will respond with a 403
   * Forbidden response.  <b>NOTE:</b> if the administrative functions modify
   * the repository rather than only provide information that is privileged for
   * administrators then access to such functions also depend on the whether
   * or not the {@link #READ_ONLY} option was specified.  A single parameter may
   * optionally be specified as <tt>true</tt> or <tt>false</tt> with
   * <tt>false</tt> simulating the absence of the option.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--enable-admin [true|false]</tt></li>
   *   <li>Command Line: <tt>-enableAdmin [true|false]</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_ENABLE_ADMIN="{true|false}"</tt></tt></li>
   * </ul>
   * </p>
   */
  ENABLE_ADMIN("--enable-admin", Set.of("-enableAdmin"),
               ENV_PREFIX + "ENABLE_ADMIN", null,
               0, "false"),

  /**
   * <p>
   * This presence of this option causes the Senzing API's to be initialized in
   * verbose mode, but its absence causes the Senzing API's in standard mode
   * (the default).  This option is used with {@link #INI_FILE},
   * {@link #INIT_FILE}, {@link #INIT_JSON} or {@link #INIT_ENV_VAR} to control
   * the Senzing API initialization.  A single parameter may optionally be
   * specified as <tt>true</tt> or <tt>false</tt> with <tt>false</tt> simulating
   * the absence of the option.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--verbose [true|false]</tt></li>
   *   <li>Command Line: <tt>-verbose [true|false]</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_VERBOSE="{true|false}"</tt></tt></li>
   * </ul>
   * </p>
   */
  VERBOSE("--verbose", Set.of("-verbose"),
          ENV_PREFIX + "VERBOSE", null,
          0, "false"),

  /**
   * <p>
   * The presence of this option causes log messages produced specifically
   * by the Senzing API Server to be reduced but does not affect messages
   * produced by the underlying Senzing API.  The absence of this option leads
   * to all log messages produced by the API Server to be generated.  A single
   * parameter may optionally be specified as <tt>true</tt> or <tt>false</tt>
   * with <tt>false</tt> simulating the absence of the option.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--quiet [true|false]</tt></li>
   *   <li>Command Line: <tt>-quiet [true|false]</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_QUIET="{true|false}"</tt></tt></li>
   * </ul>
   * </p>
   */
  QUIET("--quiet", Set.of("-quiet"),
        ENV_PREFIX + "QUIET", null,
        0, "false"),

  /**
   * <p>
   * This option is used to specify the path to a file whose timestamp is
   * monitored to determine when to shutdown.  Any change to the file's
   * timestamp will trigger a graceful shutdown.  The single parameter to this
   * option is the file path to the monitor file.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>-monitorFile {file-path}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_MONITOR_FILE="{file-path}"</tt></tt></li>
   * </ul>
   * </p>
   */
  MONITOR_FILE("--monitor-file", Set.of("-monitorFile"),
               ENV_PREFIX + "MONITOR_FILE", null,
               1),

  /**
   * <p>
   * This option sets the number of threads available for executing Senzing API
   * functions (i.e.: the number of engine threads).  The single parameter to
   * this option should be a positive integer.  If not specified, then this
   * defaults to {@link SzApiServerConstants#DEFAULT_CONCURRENCY},
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--concurrency {thread-count}</tt></li>
   *   <li>Command Line: <tt>-concurrency {thread-count}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_CONCURRENCY="{thread-count}"</tt></tt></li>
   * </ul>
   * </p>
   */
  CONCURRENCY("--concurrency", Set.of("-concurrency"),
              ENV_PREFIX + "CONCURRENCY", null,
              1, DEFAULT_CONCURRENCY_PARAM),

  /**
   * <p>
   * This option sets the maximum number of threads available for the HTTP
   * server.  The single parameter to this option should be a positive integer.
   * If not specified, then this defaults to {@link
   * SzApiServerConstants#DEFAULT_HTTP_CONCURRENCY}.  If the specified thread
   * count is less than {@link SzApiServerConstants#MINIMUM_HTTP_CONCURRENCY}
   * then an error is reported.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--http-concurrency {thread-count}</tt></li>
   *   <li>Command Line: <tt>-httpConcurrency {thread-count}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_HTTP_CONCURRENCY="{thread-count}"</tt></tt></li>
   * </ul>
   * </p>
   */
  HTTP_CONCURRENCY("--http-concurrency", Set.of("-httpConcurrency"),
              ENV_PREFIX + "HTTP_CONCURRENCY", null,
              1, DEFAULT_HTTP_CONCURRENCY_PARAM),

  /**
   * <p>
   * If leveraging the default configuration stored in the database, this option
   * is used to specify how often the API server should background check that
   * the current active config is the same as the current default config and
   * update the active config if not.  The parameter to this option is specified
   * in a number of <b>seconds</b>.  If zero is specified, then the auto-refresh
   * is disabled and it will only occur when a requested configuration element
   * is not found in the current active config.  Specifying a negative integer
   * is allowed but is used to enable a check and conditional refresh only when
   * manually requested (programmatically).  <b>NOTE:</b> This is option ignored
   * if auto-refresh is disabled because the config was specified via the
   * <tt>G2CONFIGFILE</tt> init option or if {@link #CONFIG_ID} has been
   * specified to lock to a specific configuration.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--auto-refresh-period {positive-integer-seconds|0|negative-integer}</tt></li>
   *   <li>Command Line: <tt>-autoRefreshPeriod {positive-integer-seconds|0|negative-integer}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_AUTO_REFRESH_PERIOD="{positive-integer-seconds|0|negative-integer}"</tt></tt></li>
   * </ul>
   * </p>
   */
  AUTO_REFRESH_PERIOD("--auto-refresh-period",
                      Set.of("-autoRefreshPeriod"),
                      ENV_PREFIX + "AUTO_REFRESH_PERIOD",
                      null, 1,
                      DEFAULT_CONFIG_REFRESH_PERIOD_PARAM),

  /**
   * <p>
   * This option is used to enable the CORS <tt>Access-Control-Allow-Origin</tt>
   * header for all endpoints.  If not present then CORS is disabled.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--allowed-origins {url-domain}</tt></li>
   *   <li>Command Line: <tt>-allowedOrigins {url-domain}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_ALLOWED_ORIGINS="{url-domain}"</tt></tt></li>
   * </ul>
   * </p>
   */
  ALLOWED_ORIGINS("--allowed-origins", Set.of("-allowedOrigins"),
                  ENV_PREFIX + "ALLOWED_ORIGINS",
                  null, 1),

  /**
   * <p>
   * This option is used to specify the minimum number of <b>milliseconds</b>
   * between logging of stats.  This is minimum because stats logging is
   * suppressed if the API Server is idle or active but not performing
   * activities pertaining to entity scoring.  In such cases, stats logging is
   * delayed until an activity pertaining to entity scoring is performed.  By
   * default this is set to {@link SzApiServerConstants#DEFAULT_STATS_INTERVAL}.
   * If zero (0) is specified then the logging of stats will be suppressed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--stats-interval {milliseconds}</tt></li>
   *   <li>Command Line: <tt>-statsInterval {milliseconds}</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_STATS_INTERVAL="{milliseconds}"</tt></tt></li>
   * </ul>
   * </p>
   */
  STATS_INTERVAL("--stats-interval", Set.of("-statsInterval"),
                 ENV_PREFIX + "STATS_INTERVAL", null,
                 1, DEFAULT_STATS_INTERVAL_PARAM),

  /**
   * <p>
   * The presence of this option causes the API Server to skip a performance
   * check on startup, and its absence allows the performance check to be
   * performed as normal.  A single parameter may optionally be specified as
   * <tt>true</tt> or <tt>false</tt> with <tt>false</tt> simulating the absence
   * of the option.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--skip-startup-perf [true|false]</tt></li>
   *   <li>Command Line: <tt>-skipStartupPerf [true|false]</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_SKIP_STARTUP_PERF="{true|false}"</tt></tt></li>
   * </ul>
   * </p>
   */
  SKIP_STARTUP_PERF("--skip-startup-perf", Set.of("-skipStartupPerf"),
                    ENV_PREFIX + "SKIP_STARTUP_PERF", null,
                    0, "false"),

  /**
   * The presence of this option causes the API Server to skip priming the
   * engine on startup, and its absence allows the engine priming to occur as
   * is the default behavior.  A single parameter may optionally be specified as
   * <tt>true</tt> or <tt>false</tt> with <tt>false</tt> simulating the absence
   * of the option.
   *   <li>Command Line: <tt>--skip-engine-priming [true|false]</tt></li>
   *   <li>Command Line: <tt>-skipEnginePriming [true|false]</tt></li>
   *   <li>Environment: <tt>SENZING_API_SERVER_SKIP_ENGINE_PRIMING="{true|false}"</tt></tt></li>
   * </ul>
   * </p>
   */
  SKIP_ENGINE_PRIMING("--skip-engine-priming",
                      Set.of("-skipEnginePriming"),
                      ENV_PREFIX + "SKIP_ENGINE_PRIMING", null,
                      0, "false"),

  /**
   * <p>
   * This option is used to specify the URL to an Amazon SQS queue to be used
   * for info messages.  The single parameter to this option is the URL.  If
   * this option is specified then the info queue parameters for RabbitMQ and
   * Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--sqs-info-url {url}</tt></li>
   *   <li>Command Line: <tt>-sqsInfoUrl {url}</tt></li>
   *   <li>Environment: <tt>SENZING_SQS_INFO_URL="{url}"</tt></tt></li>
   * </ul>
   * </p>
   */
  SQS_INFO_URL(
      "--sqs-info-url", Set.of("-sqsInfoUrl"),
      "SENZING_SQS_INFO_QUEUE_URL", null, 1,
      SQS_INFO_QUEUE_GROUP, URL_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the user name for connecting to RabbitMQ as
   * part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is a user name.  If this option is specified then the other options
   * required for a RabbitMQ info queue are required and the info queue
   * parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-host {username}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoHost {username}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_USERNAME="{username}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_USERNAME="{username}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_USER(
      "--rabbit-info-user", Set.of("-rabbitInfoUser"),
      "SENZING_RABBITMQ_INFO_USERNAME",
      List.of("SENZING_RABBITMQ_USERNAME"), 1,
      RABBITMQ_INFO_QUEUE_GROUP, USER_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the password for connecting to RabbitMQ as
   * part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is a password.  If this option is specified then the other options
   * required for a RabbitMQ info queue are required and the info queue
   * parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-password {password}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoPassword {password}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_PASSWORD="{password}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_PASSWORD="{password}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_PASSWORD(
      "--rabbit-info-password", Set.of("-rabbitInfoPassword"),
      "SENZING_RABBITMQ_INFO_PASSWORD",
      List.of("SENZING_RABBITMQ_PASSWORD"), 1,
      RABBITMQ_INFO_QUEUE_GROUP, PASSWORD_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the hostname for connecting to RabbitMQ as
   * part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is a hostname or IP address.  If this option is specified then the
   * other options required for a RabbitMQ info queue are required and the
   * info queue parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-host {hostname}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoHost {hostname}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_HOST="{hostname}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_HOST="{hostname}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_HOST(
      "--rabbit-info-host", Set.of("-rabbitInfoHost"),
      "SENZING_RABBITMQ_INFO_HOST",
      List.of("SENZING_RABBITMQ_HOST"), 1,
      RABBITMQ_INFO_QUEUE_GROUP, HOST_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the port number for connecting to RabbitMQ
   * as part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is a port number.  If this option is specified then the other
   * options required for a RabbitMQ info queue are required and the info queue
   * parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-port {port}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoPort {port}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_PORT="{port}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_PORT="{port}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_PORT(
      "--rabbit-info-port", Set.of("-rabbitInfoPort"),
      "SENZING_RABBITMQ_INFO_PORT",
      List.of("SENZING_RABBITMQ_PORT"), 1,
      RABBITMQ_INFO_QUEUE_GROUP, PORT_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the virtual host for connecting to RabbitMQ
   * as part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is a virtual host name.  If this option is specified then the other
   * options required for a RabbitMQ info queue are required and the info queue
   * parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-virtual-host {virtual-host}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoVirtualHost {virtual-host}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_VIRTUAL_HOST="{virtual-host}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_VIRTUAL_HOST="{virtual-host}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_VIRTUAL_HOST(
      "--rabbit-info-virtual-host", Set.of("-rabbitInfoVirtualHost"),
      "SENZING_RABBITMQ_INFO_VIRTUAL_HOST",
      List.of("SENZING_RABBITMQ_VIRTUAL_HOST"), 1,
      RABBITMQ_INFO_QUEUE_GROUP, VIRTUAL_HOST_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the exchange for connecting to RabbitMQ
   * as part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is an exchange name.  If this option is specified then the other
   * options required for a RabbitMQ info queue are required and the info queue
   * parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-exchange {exchange}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoExchange {exchange}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_EXCHANGE="{exchange}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_EXCHANGE="{exchange}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_EXCHANGE(
      "--rabbit-info-exchange", Set.of("-rabbitInfoExchange"),
      "SENZING_RABBITMQ_INFO_EXCHANGE",
      List.of("SENZING_RABBITMQ_EXCHANGE"), 1,
      RABBITMQ_INFO_QUEUE_GROUP, EXCHANGE_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the routing key for connecting to RabbitMQ
   * as part of specifying a RabbitMQ info queue.  The single parameter to this
   * option is a routing key.  If this option is specified then the other
   * options required for a RabbitMQ info queue are required and the info queue
   * parameters pertaining to SQS and Kafka are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--rabbit-info-routing-key {routing-key}</tt></li>
   *   <li>Command Line: <tt>-rabbitInfoRoutingKey {routing-key}</tt></li>
   *   <li>Environment: <tt>SENZING_RABBITMQ_INFO_ROUTING_KEY="{routing-key}"</tt></tt></li>
   * </ul>
   * </p>
   */
  RABBIT_INFO_ROUTING_KEY(
      "--rabbit-info-routing-key", Set.of("-rabbitInfoRoutingKey"),
      "SENZING_RABBITMQ_INFO_ROUTING_KEY",
      null, 1,
      RABBITMQ_INFO_QUEUE_GROUP, ROUTING_KEY_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the bootstrap servers for connecting to
   * Kafka as part of specifying a Kafka info topic.  The single parameter to
   * this option is the Kafka bootstrap servers specification (typically a
   * hostname or IP address and port number separated by a colon).  If this
   * option is specified then the {@link #KAFKA_INFO_TOPIC} option is required
   * and the info queue parameters pertaining to RabbitMQ and SQS are not
   * allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--kafka-info-bootstrap-servers {bootstrap-servers}</tt></li>
   *   <li>Command Line: <tt>-kafkaInfoBootstrapServers {bootstrap-servers}</tt></li>
   *   <li>Environment: <tt>SENZING_KAFKA_INFO_BOOTSTRAP_SERVER="{bootstrap-servers}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_KAFKA_BOOTSTRAP_SERVER="{bootstrap-servers}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  KAFKA_INFO_BOOTSTRAP_SERVER(
      "--kafka-info-bootstrap-server", Set.of("-kafkaInfoBootstrapServer"),
      "SENZING_KAFKA_INFO_BOOTSTRAP_SERVER",
      List.of("SENZING_KAFKA_BOOTSTRAP_SERVER"), 1,
      KAFKA_INFO_QUEUE_GROUP, BOOTSTRAP_SERVERS_PROPERTY_KEY, false),

  /**
   * <p>
   * This option is used to specify the <b>optional</b> group ID to set when
   * connecting to Kafka as part of specifying a Kafka info topic.  The single
   * parameter to this option is the Kafka group ID.  If this option is
   * specified then the {@link #KAFKA_INFO_BOOTSTRAP_SERVER} and {@link
   * #KAFKA_INFO_TOPIC} options are required and the info queue parameters
   * pertaining to RabbitMQ and SQS are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--kafka-info-group {group-id}</tt></li>
   *   <li>Command Line: <tt>-kafkaInfoGroup {group-id}</tt></li>
   *   <li>Environment: <tt>SENZING_KAFKA_INFO_GROUP="{group-id}"</tt></tt></li>
   *   <li>Environment: <tt>SENZING_KAFKA_GROUP="{group-id}" (fallback)</tt></tt></li>
   * </ul>
   * </p>
   */
  KAFKA_INFO_GROUP(
      "--kafka-info-group", Set.of("-kafkaInfoGroup"),
      "SENZING_KAFKA_INFO_GROUP",
      List.of("SENZING_KAFKA_GROUP"), 1, KAFKA_INFO_QUEUE_GROUP,
      GROUP_ID_PROPERTY_KEY, true),

  /**
   * <p>
   * This option is used to specify the topic when connecting to Kafka as part
   * of specifying a Kafka info topic.  The single parameter to this option is
   * the Kafka topic.  If this option is specified then the {@link
   * #KAFKA_INFO_BOOTSTRAP_SERVER} option is required and the info queue
   * parameters pertaining to RabbitMQ and SQS are not allowed.
   * </p>
   * <p>
   * This option can be specified in the following ways:
   * <ul>
   *   <li>Command Line: <tt>--kafka-info-topic {topic-name}</tt></li>
   *   <li>Command Line: <tt>-kafkaInfoTopic {topic-name}</tt></li>
   *   <li>Environment: <tt>SENZING_KAFKA_INFO_TOPIC="{topic-name}"</tt></tt></li>
   * </ul>
   * </p>
   */
  KAFKA_INFO_TOPIC(
      "--kafka-info-topic", Set.of("-kafkaInfoTopic"),
      "SENZING_KAFKA_INFO_TOPIC", null, 1,
      KAFKA_INFO_QUEUE_GROUP, TOPIC_PROPERTY_KEY, false);

  /**
   * The {@link Map} of {@link SzApiServerOption} keys to unmodifiable
   * {@link Set} values containing the {@link SzApiServerOption} values that
   * conflict with the key {@link SzApiServerOption} value.
   */
  private static Map<SzApiServerOption, Set<SzApiServerOption>> CONFLICTING_OPTIONS;

  /**
   * The {@link Map} of {@link SzApiServerOption} keys to <b>unmodifiable</b>
   * {@link Set} values containing the {@link SzApiServerOption} values that
   * are alternatives to the key {@link SzApiServerOption} value.
   */
  private static Map<SzApiServerOption, Set<SzApiServerOption>> ALTERNATIVE_OPTIONS;

  /**
   * The {@link Map} of {@link String} option flags to their corresponding
   * {@link SzApiServerOption} values.
   */
  private static Map<String, SzApiServerOption> OPTIONS_BY_FLAG;

  /**
   * The {@link Map} of {@link SzApiServerOption} keys to <b>unmodifiable</b>
   * {@link Set} values containing alternative {@link Set}'s of {@link
   * SzApiServerOption} that the key option is dependent on if specified.
   */
  private static Map<SzApiServerOption, Set<Set<SzApiServerOption>>> DEPENDENCIES;

  /**
   * Flag indicating if this option is considered a "primary" option.
   */
  private boolean primary;

  /**
   * Flag indicating if this option is considered a deprecated option.
   */
  private boolean deprecated;

  /**
   * The primary command-line flag.
   */
  private String cmdLineFlag;

  /**
   * The {@link Set} of synonym command-line flags for this option.
   */
  private Set<String> synonymFlags;

  /**
   * The optional environment variable associated with option.
   */
  private String envVariable;

  /**
   * The environment variable fallbacks for this option.
   */
  private List<String> envFallbacks;

  /**
   * The minimum number of parameters that can be specified for this option.
   */
  private int minParamCount;

  /**
   * The maximum number of parmaeters that can be specifeid for this option.
   */
  private int maxParamCount;

  /**
   * The {@link List} o {@link String} default parameters for this option.  This
   * is <tt>null</tt> if no default and an empty {@link List} if the option is
   * specified by default with no parameters.
   */
  private List<String> defaultParameters;

  /**
   * The group name for the option group that this parameter belongs to.
   */
  private String groupName;

  /**
   * The property key to map the option to for the group for initializing a
   * sub-object with the options in that group.
   */
  private String groupPropertyKey;

  /**
   * The property indicating if the option is not required for the validity of
   * the group to which it belongs.
   */
  private boolean groupOptional;

  SzApiServerOption(String      cmdLineFlag,
                    Set<String> synonymFlags,
                    int         parameterCount)
  {
    this(cmdLineFlag,
         synonymFlags,
         null,
         null,
         false,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         false,
         null,
         null,
         true);

  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    int           parameterCount,
                    String...     defaultParams) {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         false,
         parameterCount,
         List.of(defaultParams),
         false,
         null,
         null,
         true);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    int           minParamCount,
                    int           maxParamCount,
                    String...     defaultParams) {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         false,
         minParamCount,
         maxParamCount,
         List.of(defaultParams),
         false,
         null,
         null,
         true);
  }

  SzApiServerOption(String      cmdLineFlag,
                    Set<String> synonymFlags,
                    int         parameterCount,
                    String      groupName,
                    String      groupPropertyKey,
                    boolean     groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         null,
         null,
         false,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         false,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    int           parameterCount,
                    String        groupName,
                    String        groupPropertyKey,
                    boolean       groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         false,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         false,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String      cmdLineFlag,
                    Set<String> synonymFlags,
                    boolean     primary,
                    int         parameterCount)
  {
    this(cmdLineFlag,
         synonymFlags,
         null,
         null,
         primary,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         false,
         null,
         null,
         true);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    boolean       primary,
                    int           parameterCount)
  {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         primary,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         false,
         null,
         null,
         true);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    int           parameterCount)
  {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         false,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         false,
         null,
         null,
         true);
  }

  SzApiServerOption(String      cmdLineFlag,
                    Set<String> synonymFlags,
                    int         parameterCount,
                    String...   defaultParams)
  {
    this(cmdLineFlag,
         synonymFlags,
         null,
         null,
         false,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         List.of(defaultParams),
         false,
         null,
         null,
         true);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    boolean       primary,
                    int           parameterCount,
                    boolean       deprecated,
                    String        groupName,
                    String        groupPropertyKey,
                    boolean       groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         null,
         null,
         primary,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         Collections.emptyList(),
         deprecated,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    boolean       primary,
                    int           parameterCount,
                    boolean       deprecated,
                    String        groupName,
                    String        groupPropertyKey,
                    boolean       groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         primary,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         deprecated,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    boolean       primary,
                    int           parameterCount,
                    List<String>  defaultParameters,
                    boolean       deprecated,
                    String        groupName,
                    String        groupPropertyKey,
                    boolean       groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         primary,
         parameterCount < 0 ? 0 : parameterCount,
         parameterCount,
         defaultParameters,
         deprecated,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String      cmdLineFlag,
                    Set<String> synonymFlags,
                    boolean     primary,
                    int         minParameterCount,
                    int         maxParameterCount,
                    boolean     deprecated,
                    String      groupName,
                    String      groupPropertyKey,
                    boolean     groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         null,
         null,
         primary,
         minParameterCount,
         maxParameterCount,
         null,
         deprecated,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    boolean       primary,
                    int           minParameterCount,
                    int           maxParameterCount,
                    boolean       deprecated,
                    String        groupName,
                    String        groupPropertyKey,
                    boolean       groupOptional)
  {
    this(cmdLineFlag,
         synonymFlags,
         envVariable,
         envFallbacks,
         primary,
         minParameterCount,
         maxParameterCount,
         null,
         deprecated,
         groupName,
         groupPropertyKey,
         groupOptional);
  }

  SzApiServerOption(String        cmdLineFlag,
                    Set<String>   synonymFlags,
                    String        envVariable,
                    List<String>  envFallbacks,
                    boolean       primary,
                    int           minParameterCount,
                    int           maxParameterCount,
                    List<String>  defaultParameters,
                    boolean       deprecated,
                    String        groupName,
                    String        groupPropertyKey,
                    boolean       groupOptional)
  {
    this.cmdLineFlag        = cmdLineFlag;
    this.synonymFlags       = Set.copyOf(synonymFlags);
    this.envVariable        = envVariable;
    this.primary            = primary;
    this.minParamCount      = minParameterCount;
    this.maxParamCount      = maxParameterCount;
    this.deprecated         = deprecated;
    this.groupName          = groupName;
    this.groupPropertyKey   = groupPropertyKey;
    this.groupOptional      = groupOptional;
    this.envFallbacks       = (envFallbacks == null)
        ? Collections.emptyList() : List.copyOf(envFallbacks);
    this.defaultParameters  = (defaultParameters == null)
        ? Collections.emptyList() : List.copyOf(defaultParameters);
  }

  @Override
  public int getMinimumParameterCount() { return this.minParamCount; }

  @Override
  public int getMaximumParameterCount() { return this.maxParamCount; }

  @Override
  public List<String> getDefaultParameters() {
    return this.defaultParameters;
  }

  public boolean isPrimary() {
    return this.primary;
  }

  @Override
  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String getCommandLineFlag() {
    return this.cmdLineFlag;
  }

  @Override
  public Set<String> getSynonymFlags() {
    return this.synonymFlags;
  }

  @Override
  public String getEnvironmentVariable() {
    return this.envVariable;
  }

  @Override
  public List<String> getEnvironmentFallbacks() {
    return this.envFallbacks;
  }

  public String getGroupName() {
    return this.groupName;
  }

  public String getGroupPropertyKey() {
    return this.groupPropertyKey;
  }

  public boolean isGroupOptional() {
    return this.groupOptional;
  }

  @Override
  public Set<SzApiServerOption> getConflicts() {
    return CONFLICTING_OPTIONS.get(this);
  }

  public Set<SzApiServerOption> getAlternatives() {
    return ALTERNATIVE_OPTIONS.get(this);
  }

  @Override
  public Set<Set<SzApiServerOption>> getDependencies() {
    Set<Set<SzApiServerOption>> set = DEPENDENCIES.get(this);
    return (set == null) ? Collections.emptySet() : set;
  }

  public static SzApiServerOption lookup(String commandLineFlag) {
    return OPTIONS_BY_FLAG.get(commandLineFlag.toLowerCase());
  }

  static {
    try {
      Map<SzApiServerOption,Set<SzApiServerOption>> conflictMap = new LinkedHashMap<>();
      Map<SzApiServerOption,Set<SzApiServerOption>> altMap = new LinkedHashMap<>();
      Map<String, SzApiServerOption> lookupMap = new LinkedHashMap<>();

      for (SzApiServerOption option : SzApiServerOption.values()) {
        conflictMap.put(option, new LinkedHashSet<>());
        altMap.put(option, new LinkedHashSet<>());
        lookupMap.put(option.getCommandLineFlag().toLowerCase(), option);
      }
      SzApiServerOption[] exclusiveOptions = { HELP, VERSION };
      for (SzApiServerOption option : SzApiServerOption.values()) {
        for (SzApiServerOption exclOption : exclusiveOptions) {
          Set<SzApiServerOption> set = conflictMap.get(exclOption);
          set.add(option);
          set = conflictMap.get(option);
          set.add(exclOption);
        }
      }

      SzApiServerOption[] initOptions
          = { INI_FILE, INIT_ENV_VAR, INIT_FILE, INIT_JSON };

      for (SzApiServerOption option1 : initOptions) {
        for (SzApiServerOption option2 : initOptions) {
          if (option1 != option2) {
            Set<SzApiServerOption> set = conflictMap.get(option1);
            set.add(option2);
          }
        }
      }

      Set<SzApiServerOption> kafkaInfoOptions = Set.of(
          KAFKA_INFO_BOOTSTRAP_SERVER,
          KAFKA_INFO_GROUP,
          KAFKA_INFO_TOPIC);

      Set<SzApiServerOption> rabbitInfoOptions = Set.of(
          RABBIT_INFO_USER,
          RABBIT_INFO_PASSWORD,
          RABBIT_INFO_HOST,
          RABBIT_INFO_PORT,
          RABBIT_INFO_VIRTUAL_HOST,
          RABBIT_INFO_EXCHANGE,
          RABBIT_INFO_ROUTING_KEY);

      Set<SzApiServerOption> sqsInfoOptions = Set.of(SQS_INFO_URL);

      // enforce that we only have one info queue
      for (SzApiServerOption option: kafkaInfoOptions) {
        Set<SzApiServerOption> conflictSet = conflictMap.get(option);
        conflictSet.addAll(rabbitInfoOptions);
        conflictSet.addAll(sqsInfoOptions);
      }
      for (SzApiServerOption option: rabbitInfoOptions) {
        Set<SzApiServerOption> conflictSet = conflictMap.get(option);
        conflictSet.addAll(kafkaInfoOptions);
        conflictSet.addAll(sqsInfoOptions);
      }
      for (SzApiServerOption option: sqsInfoOptions) {
        Set<SzApiServerOption> conflictSet = conflictMap.get(option);
        conflictSet.addAll(kafkaInfoOptions);
        conflictSet.addAll(rabbitInfoOptions);
      }

      Set<SzApiServerOption> readOnlyConflicts = conflictMap.get(READ_ONLY);
      readOnlyConflicts.addAll(kafkaInfoOptions);
      readOnlyConflicts.addAll(rabbitInfoOptions);
      readOnlyConflicts.addAll(sqsInfoOptions);

      Set<SzApiServerOption> iniAlts = altMap.get(INI_FILE);
      iniAlts.add(INIT_ENV_VAR);
      iniAlts.add(INIT_FILE);
      iniAlts.add(INIT_JSON);

      Map<SzApiServerOption, Set<Set<SzApiServerOption>>> dependencyMap
          = new LinkedHashMap<>();

      // handle dependencies for groups of options that go together
      Map<String, Set<SzApiServerOption>> groups = new LinkedHashMap<>();
      for (SzApiServerOption option: SzApiServerOption.values()) {
        String groupName = option.getGroupName();
        if (groupName == null) continue;
        Set<SzApiServerOption> set = groups.get(groupName);
        if (set == null) {
          set = new LinkedHashSet<>();
          groups.put(groupName, set);
        }
        set.add(option);
      }

      // create the dependencies using the groupings
      groups.forEach((groupName, group) -> {
        for (SzApiServerOption option : group) {
          Set<SzApiServerOption> others = new LinkedHashSet<>(group);

          // remove self from the group (can't depend on itself)
          others.remove(option);

          // remove any options that are not required
          for (SzApiServerOption opt: group) {
            if (opt.isGroupOptional()) others.remove(opt);
          }

          // make the others set unmodifiable
          others = Collections.unmodifiableSet(others);

          // add the dependency
          dependencyMap.put(option, Set.of(others));
        }
      });

      CONFLICTING_OPTIONS = recursivelyUnmodifiableMap(conflictMap);
      ALTERNATIVE_OPTIONS = recursivelyUnmodifiableMap(altMap);
      OPTIONS_BY_FLAG = Collections.unmodifiableMap(lookupMap);
      DEPENDENCIES = Collections.unmodifiableMap(dependencyMap);

    } catch (Exception e) {
      e.printStackTrace();
      throw new ExceptionInInitializerError(e);
    }
  }
}
