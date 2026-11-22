import { Page, Locator, expect } from '@playwright/test';

export class RegisterPage {
  readonly page: Page;
  readonly usernameInput: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly roleSelect: Locator;
  readonly submitButton: Locator;
  readonly errorAlert: Locator;
  readonly successAlert: Locator;
  readonly signInLink: Locator;
  readonly createHeading: Locator;

  constructor(page: Page) {
    this.page = page;
    this.usernameInput = page.locator('#username');
    this.emailInput = page.locator('#email');
    this.passwordInput = page.locator('#password');
    this.roleSelect = page.locator('#role');
    this.submitButton = page.locator('button[type="submit"]');
    this.errorAlert = page.locator('.alert.alert-danger');
    this.successAlert = page.locator('.alert:not(.alert-danger)');
    this.signInLink = page.getByRole('link', { name: 'Sign In' });
    this.createHeading = page.getByRole('heading', { name: 'Create an Account' });
  }

  async goto() {
    await this.page.goto('/signup');
    await this.waitForLoaded();
  }

  /**
   * Senior SDET Pattern: Wait for page mount & CSS animations to settle 100%
   */
  async waitForLoaded() {
    await this.createHeading.waitFor({ state: 'visible' });
    await this.usernameInput.waitFor({ state: 'visible' });
    await this.submitButton.waitFor({ state: 'visible' });
    await this.page.evaluate(() => Promise.all(document.getAnimations().map(a => a.finished)));
  }

  async fillRegistrationForm(username: string, email: string, password: string, role: string = 'ROLE_EMPLOYEE') {
    await this.usernameInput.fill(username);
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.roleSelect.selectOption(role);
  }

  async submit() {
    await this.submitButton.click();
  }

  async register(username: string, email: string, password: string, role: string = 'ROLE_EMPLOYEE') {
    await this.fillRegistrationForm(username, email, password, role);
    await this.submit();
  }

  /**
   * Dynamic wait for success banner to be fully opaque and settled
   */
  async waitForSuccess() {
    await this.successAlert.waitFor({ state: 'visible' });
    await expect(this.successAlert).toHaveCSS('opacity', '1');
    await this.page.evaluate(() => Promise.all(document.getAnimations().map(a => a.finished)));
  }

  /**
   * Dynamic wait for duplicate error banner to be fully opaque and button back to idle
   */
  async waitForError() {
    await this.errorAlert.waitFor({ state: 'visible' });
    await expect(this.errorAlert).toHaveCSS('opacity', '1');
    await expect(this.submitButton).toHaveText('Sign Up');
    await this.page.evaluate(() => Promise.all(document.getAnimations().map(a => a.finished)));
  }
}
