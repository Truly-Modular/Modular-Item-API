package smartin.miapi.material;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.config.FunctionDictionaryIfc;
import com.ezylang.evalex.config.OperatorDictionaryIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.InfixOperator;
import com.ezylang.evalex.parser.Token;

import java.math.BigDecimal;

import static com.ezylang.evalex.operators.OperatorIfc.OPERATOR_PRECEDENCE_MULTIPLICATIVE;
import static com.ezylang.evalex.operators.OperatorIfc.OPERATOR_PRECEDENCE_POWER;

public class EvalExResolverStuff {
    public static ExpressionConfiguration configuration;

    static {
        FunctionDictionaryIfc dictionaryIfc = ExpressionConfiguration.defaultConfiguration().getFunctionDictionary();
        OperatorDictionaryIfc operatorIfc = ExpressionConfiguration.defaultConfiguration().getOperatorDictionary();
        operatorIfc.addOperator("/", new DivisionByZeroAllowed());
        operatorIfc.addOperator("^", new PowerOperatorWorks());
        dictionaryIfc.addFunction("ceil", dictionaryIfc.getFunction("ceiling"));
        configuration = ExpressionConfiguration.builder()
                .allowOverwriteConstants(true)
                .arraysAllowed(true)
                .dataAccessorSupplier(ExpressionConfiguration.defaultConfiguration().getDataAccessorSupplier())
                .decimalPlacesRounding(ExpressionConfiguration.DECIMAL_PLACES_ROUNDING_UNLIMITED)
                .defaultConstants(ExpressionConfiguration.StandardConstants)
                .implicitMultiplicationAllowed(true)
                .mathContext(ExpressionConfiguration.DEFAULT_MATH_CONTEXT)
                .powerOfPrecedence(50)
                .stripTrailingZeros(true)
                .structuresAllowed(true)
                .singleQuoteStringLiteralsAllowed(false)
                .functionDictionary(dictionaryIfc)
                .operatorDictionary(operatorIfc)
                .build();
    }

    @InfixOperator(precedence = OPERATOR_PRECEDENCE_MULTIPLICATIVE)
    public static class DivisionByZeroAllowed extends AbstractOperator {

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token operatorToken, EvaluationValue... operands)
                throws EvaluationException {
            EvaluationValue leftOperand = operands[0];
            EvaluationValue rightOperand = operands[1];

            if (leftOperand.isNumberValue() && rightOperand.isNumberValue()) {
                if (rightOperand.getNumberValue().doubleValue() == 0 || leftOperand.getNumberValue().doubleValue() == 0) {
                    return EvaluationValue.numberValue(BigDecimal.ZERO);
                }
                try {
                    BigDecimal decimal = leftOperand
                            .getNumberValue()
                            .divide(
                                    rightOperand.getNumberValue(), expression.getConfiguration().getMathContext());
                    return expression.convertValue(decimal);
                } catch (Exception e) {
                    return EvaluationValue.numberValue(BigDecimal.valueOf(0.0));
                }
            } else {
                return EvaluationValue.numberValue(BigDecimal.valueOf(0.0));
            }
        }
    }

    /**
     * idk it keept crashing but replacing it with the exact same code fixed it? idfk
     */
    @InfixOperator(precedence = OPERATOR_PRECEDENCE_POWER, leftAssociative = false)
    public static class PowerOperatorWorks extends AbstractOperator {

        @Override
        public EvaluationValue evaluate(
                Expression expression, Token operatorToken, EvaluationValue... operands)
                throws EvaluationException {
            EvaluationValue leftOperand = operands[0];
            EvaluationValue rightOperand = operands[1];

            if (leftOperand.isNumberValue() && rightOperand.isNumberValue()) {
                double v1 = leftOperand.getNumberValue().doubleValue();
                double v2 = rightOperand.getNumberValue().doubleValue();
                double result;
                if (v1 < 0) {
                    result = -Math.pow(-v1, v2);
                } else {
                    result = Math.pow(v1, v2);
                }
                return EvaluationValue.numberValue(BigDecimal.valueOf(result));
            } else {
                throw EvaluationException.ofUnsupportedDataTypeInOperation(operatorToken);
            }
        }

        @Override
        public int getPrecedence(ExpressionConfiguration configuration) {
            return configuration.getPowerOfPrecedence();
        }
    }
}
