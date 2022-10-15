package efrem.datamanager.user.validator;

import edu.vt.middleware.password.*;

import java.util.ArrayList;
import java.util.List;

public class StrongPasswordValidator {

    public static String result(String password) {
        LengthRule lengthRule = new LengthRule(); // passwords must have at least 8 characters
        lengthRule.setMinimumLength(8);
        CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
        charRule.getRules().add(new DigitCharacterRule(1));
        charRule.getRules().add(new NonAlphanumericCharacterRule(1));
        charRule.getRules().add(new UppercaseCharacterRule(1));
        charRule.getRules().add(new LowercaseCharacterRule(1));
        charRule.setNumberOfCharacteristics(4);
        AlphabeticalSequenceRule alphaSeqRule = new AlphabeticalSequenceRule();
        QwertySequenceRule qwertySeqRule = new QwertySequenceRule();
        List<Rule> ruleList = new ArrayList<Rule>();
        ruleList.add(lengthRule);
        ruleList.add(charRule);
        ruleList.add(alphaSeqRule);
        ruleList.add(qwertySeqRule);
        PasswordValidator validator = new PasswordValidator(ruleList);
        PasswordData passwordData = new PasswordData(new Password(password));
        RuleResult ruleResult = validator.validate(passwordData);
        String passwordErrorMessage = "";
        if (!ruleResult.isValid()) {
            passwordErrorMessage = "Password is not strong.\n";
            for (String msg : validator.getMessages(ruleResult)) {
                if (msg.contains("character rules"))
                    continue;
                passwordErrorMessage += msg;
                passwordErrorMessage += '\n';

            }
        }

        return passwordErrorMessage;
    }
}
