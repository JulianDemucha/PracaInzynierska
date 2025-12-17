/* eslint-env jest */
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import AuthModal from './AuthModal';
import { useAuth } from "../../context/useAuth.js";

jest.mock("../../context/useAuth.js", () => ({
    useAuth: jest.fn(),
}));

describe('AuthModal - Testy Walidacji', () => {

    const defaultAuthContext = {
        isModalOpen: true,
        modalView: 'login',
        closeModal: jest.fn(),
        switchToLogin: jest.fn(),
        switchToRegister: jest.fn(),
        login: jest.fn(),
        register: jest.fn(),
        loading: false,
        error: null
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('1. Pokazuje błąd "Wpisz adres email", gdy email jest pusty (Login)', () => {
        useAuth.mockReturnValue({
            ...defaultAuthContext,
            modalView: 'login'
        });

        render(<AuthModal />);

        const submitButton = screen.getByRole('button', { name: /zaloguj/i });

        fireEvent.click(submitButton);

        expect(screen.getByText(/wpisz adres email/i)).toBeInTheDocument();
    });

    test('2. Pokazuje błąd "Wpisz hasło", gdy hasło jest puste (Login)', () => {
        useAuth.mockReturnValue({
            ...defaultAuthContext,
            modalView: 'login'
        });

        render(<AuthModal />);

        const submitButton = screen.getByRole('button', { name: /zaloguj/i });
        fireEvent.click(submitButton);

        expect(screen.getByText(/wpisz hasło/i)).toBeInTheDocument();
    });

    test('3. Pokazuje błąd "Wprowadź nazwę użytkownika", gdy username pusty (Rejestracja)', () => {
        useAuth.mockReturnValue({
            ...defaultAuthContext,
            modalView: 'register'
        });

        render(<AuthModal />);

        const registerButton = screen.getByRole('button', { name: /zarejestruj/i });

        fireEvent.click(registerButton);

        expect(screen.getByText(/wprowadź nazwę użytkownika/i)).toBeInTheDocument();
    });
});