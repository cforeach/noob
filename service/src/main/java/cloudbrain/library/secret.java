/*
 * secret.java
 * Copyright (C) 2017 CloudBrain <chenp@>
 *
 * Distributed under terms of the CloudBrain license.
 */

package cloudbrain.library;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.RuntimeException;
import java.lang.String;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class secret
{
  private secret() { }

  public static String getSecretFile(String name) {
    return "/run/secrets/" + name;
  }

  public static String getSecretText(String name) {
    Path path = Paths.get(getSecretFile(name));
    try {
      return new String(Files.readAllBytes(path)).trim();
    } catch (FileNotFoundException e) {
      return "";
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}