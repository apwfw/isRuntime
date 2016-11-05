package de.intarsys.tools.authenticate;

import de.intarsys.tools.prompter.IPrompter;

public class CachedPasswordProvider implements IPasswordProvider, IPrompter {

  final private IPasswordProvider passwordProvider;
  private char[] password;

  public CachedPasswordProvider(IPasswordProvider passwordProvider) {
    super();
    this.passwordProvider = passwordProvider;
  }

  @Override
  public char[] getPassword() {
    if (password != null) {
      return password;
    }
    password = getPasswordProvider().getPassword();
    return password;
  }

  public IPasswordProvider getPasswordProvider() {
    return passwordProvider;
  }

  public void setMessage(String message) {
    if (getPasswordProvider() instanceof IPrompter) {
      ((IPrompter) passwordProvider).setMessage(message);
    }
  }

  public void setTitle(String title) {
    if (getPasswordProvider() instanceof IPrompter) {
      ((IPrompter) passwordProvider).setTitle(title);
    }
  }
}