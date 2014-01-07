package org.multibit.hd.ui.views.wizards.welcome;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import org.multibit.hd.core.api.seed_phrase.SeedPhraseSize;
import org.multibit.hd.ui.events.view.ViewEvents;
import org.multibit.hd.ui.i18n.Languages;
import org.multibit.hd.ui.views.wizards.AbstractWizardModel;
import org.multibit.hd.ui.views.wizards.WizardButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.multibit.hd.ui.views.wizards.welcome.WelcomeWizardState.*;

/**
 * <p>Model object to provide the following to welcome wizard:</p>
 * <ul>
 * <li>Storage of panel data</li>
 * <li>State transition management</li>
 * </ul>
 * <p>Example:</p>
 * <pre>
 * </pre>
 *
 * @since 0.0.1
 *  
 */
public class WelcomeWizardModel extends AbstractWizardModel<WelcomeWizardState> {

  private static final Logger log = LoggerFactory.getLogger(WelcomeWizardModel.class);

  /**
   * The "select wallet" radio button choice (as a state)
   */
  private String localeCode = Languages.currentLocale().getLanguage();

  /**
   * The "select wallet" radio button choice (as a state)
   */
  private WelcomeWizardState selectWalletChoice = WelcomeWizardState.CREATE_WALLET_SEED_PHRASE;

  /**
   * The seed phrase for the wallet as generated
   */
  private List<String> actualSeedPhrase = Lists.newArrayList();

  /**
   * The seed phrase for the wallet from the user
   */
  private List<String> userSeedPhrase = Lists.newArrayList();

  /**
   * @param state The state object
   */
  public WelcomeWizardModel(WelcomeWizardState state) {
    super(state);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void update(Optional panelModel) {

    // No state transitions occur in this method

    // TODO Consider migrating state into dedicated objects

    switch (state) {
      case WELCOME:
        localeCode = (String) panelModel.get();
        break;
      case SELECT_WALLET:
        selectWalletChoice = (WelcomeWizardState) panelModel.get();
        break;
      case RESTORE_WALLET:
        break;
      case HARDWARE_WALLET:
        break;
      case CREATE_WALLET_SEED_PHRASE:
        actualSeedPhrase = (List<String>) panelModel.get();
        // TODO remove this
        for (String word : actualSeedPhrase) {
          System.out.print(word + " ");
        }
        System.out.println(", length="+actualSeedPhrase.size());
        break;
      case CONFIRM_WALLET_SEED_PHRASE:
        // Require a decision event
        userSeedPhrase = (List<String>) panelModel.get();
        // TODO remove this
        System.out.println("Comparing:");
        for (String word : actualSeedPhrase) {
          System.out.print(word + " ");
        }
        System.out.println(", with");
        for (String word : userSeedPhrase) {
          System.out.print(word + " ");
        }
        System.out.println(", actual length="+actualSeedPhrase.size());
        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
        ViewEvents.fireWizardEnableButton(
          CONFIRM_WALLET_SEED_PHRASE.name(),
          WizardButton.NEXT,
          userSeedPhrase.equals(actualSeedPhrase));
        break;
    }

  }

  @Override
  public void next() {

    switch (state) {
      case WELCOME:
        state = SELECT_WALLET;
        break;
      case SELECT_WALLET:
        state = selectWalletChoice;
        break;
      case CREATE_WALLET_SEED_PHRASE:
        state = CONFIRM_WALLET_SEED_PHRASE;
        Preconditions.checkState(SeedPhraseSize.isValid(actualSeedPhrase.size()),"'actualSeedPhrase' is not a valid length");
        ViewEvents.fireWizardEnableButton(CONFIRM_WALLET_SEED_PHRASE.name(), WizardButton.NEXT, false);
        break;
      case CONFIRM_WALLET_SEED_PHRASE:
        Preconditions.checkState(SeedPhraseSize.isValid(userSeedPhrase.size()),"'userSeedPhrase' is not a valid length");
        state = CREATE_WALLET_PASSWORD;
        break;
      case RESTORE_WALLET:
      state = CONFIRM_WALLET_SEED_PHRASE;
      break;
      case HARDWARE_WALLET:
      state = CONFIRM_WALLET_SEED_PHRASE;
      break;
    }

  }

  @Override
  public void previous() {

    switch (state) {
      case WELCOME:
        state = WELCOME;
        break;
      case SELECT_WALLET:
        state = WELCOME;
        break;
      case CREATE_WALLET_SEED_PHRASE:
        state = SELECT_WALLET;
        break;
      case RESTORE_WALLET:
        state = SELECT_WALLET;
        break;
      case HARDWARE_WALLET:
        state = SELECT_WALLET;
        break;
    }
  }

  @Override
  public String getPanelName() {
    return state.name();
  }

  /**
   * @return The user selection for the locale
   */
  public String getLocaleCode() {
    return localeCode;
  }

  /**
   * @return The "select wallet" radio button choice
   */
  public WelcomeWizardState getSelectWalletChoice() {
    return selectWalletChoice;
  }

  /**
   * @return The actual generated seed phrase
   */
  public List<String> getActualSeedPhrase() {
    return actualSeedPhrase;
  }

  /**
   * @return The user entered seed phrase
   */
  public List<String> getUserSeedPhrase() {
    return userSeedPhrase;
  }
}
