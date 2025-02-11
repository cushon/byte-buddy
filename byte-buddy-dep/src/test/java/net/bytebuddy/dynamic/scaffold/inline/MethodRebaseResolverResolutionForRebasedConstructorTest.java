package net.bytebuddy.dynamic.scaffold.inline;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeList;
import net.bytebuddy.implementation.bytecode.StackSize;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.objectweb.asm.Opcodes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class MethodRebaseResolverResolutionForRebasedConstructorTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @Rule
    public MethodRule mockitoRule = MockitoJUnit.rule().silent();

    @Mock
    private MethodDescription.InDefinedShape methodDescription;

    @Mock
    private TypeDescription.Generic typeDescription, parameterType, placeholderType, returnType;

    @Mock
    private TypeDescription rawTypeDescription, rawParameterType, rawReturnType, otherPlaceHolderType, rawPlaceholderType;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        when(typeDescription.asErasure()).thenReturn(rawTypeDescription);
        when(methodDescription.isConstructor()).thenReturn(true);
        when(methodDescription.getDeclaringType()).thenReturn(rawTypeDescription);
        when(methodDescription.getReturnType()).thenReturn(returnType);
        when(methodDescription.getParameters()).thenReturn(new ParameterList.Explicit.ForTypes(methodDescription, parameterType));
        when(placeholderType.getStackSize()).thenReturn(StackSize.ZERO);
        when(placeholderType.asErasure()).thenReturn(rawPlaceholderType);
        when(placeholderType.asGenericType()).thenReturn(placeholderType);
        when(rawPlaceholderType.asGenericType()).thenReturn(placeholderType);
        when(parameterType.asGenericType()).thenReturn(parameterType);
        when(parameterType.getStackSize()).thenReturn(StackSize.ZERO);
        when(rawParameterType.getStackSize()).thenReturn(StackSize.ZERO);
        when(parameterType.asErasure()).thenReturn(rawParameterType);
        when(parameterType.accept(any(TypeDescription.Generic.Visitor.class))).thenReturn(parameterType);
        when(rawParameterType.asGenericType()).thenReturn(parameterType);
        when(methodDescription.getInternalName()).thenReturn(FOO);
        when(methodDescription.getDescriptor()).thenReturn(QUX);
        when(rawTypeDescription.getInternalName()).thenReturn(BAR);
        when(rawPlaceholderType.getDescriptor()).thenReturn(BAZ);
        when(otherPlaceHolderType.getDescriptor()).thenReturn(FOO);
        when(returnType.asErasure()).thenReturn(rawReturnType);
    }

    @Test
    public void testPreservation() throws Exception {
        MethodRebaseResolver.Resolution resolution = MethodRebaseResolver.Resolution.ForRebasedConstructor.of(methodDescription, rawPlaceholderType);
        assertThat(resolution.isRebased(), is(true));
        assertThat(resolution.getResolvedMethod().getDeclaringType(), is(rawTypeDescription));
        assertThat(resolution.getResolvedMethod().getInternalName(), is(MethodDescription.CONSTRUCTOR_INTERNAL_NAME));
        assertThat(resolution.getResolvedMethod().getModifiers(), is(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_PRIVATE));
        assertThat(resolution.getResolvedMethod().getReturnType(), is(TypeDescription.Generic.VOID));
        assertThat(resolution.getResolvedMethod().getParameters(), is((ParameterList<ParameterDescription.InDefinedShape>) new ParameterList.Explicit
                .ForTypes(resolution.getResolvedMethod(), parameterType, placeholderType)));
        assertThat(resolution.getAppendedParameters(), equalTo((TypeList) new TypeList.Explicit(rawPlaceholderType)));
    }
}
