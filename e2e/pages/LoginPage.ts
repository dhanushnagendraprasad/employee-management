import { Page, Locator, expect } from '@playwright/test';

export class LoginPage {
  readonly page: Page;
  readonly usernameInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;
  readonly errorAlert: Locator;
  readonly signUpLink: Locator;
  readonly welcomeHeading: Locator;

  constructor(page: Page) {
    this.page = page;
    this.usernameInput = page.locator('#username');
    this.passwordInput = page.locator('#password');
    this.submitButton = page.locator('button[type="submit"]');
    this.errorAlert = page.locator('.alert.alert-danger');
    this.signUpLink = page.getByRole('link', { name: 'Sign Up' });
    this.welcomeHeading = page.getByRole('heading', { name: 'Welcome Back' });
  }

  async goto() {
    await this.page.goto('/login');
    await this.waitForLoaded();
  }

  /**
   * Senior SDET Pattern: Wait for incoming page to be fully mounted,
   * visible, and for all CSS animations (fade-in, slide) to finish 100%.
   * Eliminates mid-transition blank or faint screenshots.
   */
  async waitForLoaded() {
    await this.welcomeHeading.waitFor({ state: 'visible' });
    await this.usernameInput.waitFor({ state: 'visible' });
    await this.submitButton.waitFor({ state: 'visible' });
    await this.page.evaluate(() => Promise.all(document.getAnimations().map(a => a.finished)));
  }

  async enterUsername(username: string) {
    await this.usernameInput.fill(username);
  }

  async enterPassword(password: string) {
    await this.passwordInput.fill(password);
  }

  async submit() {
    await this.submitButton.click();
  }

  async login(username: string, password: string) {
    await this.enterUsername(username);
    await this.enterPassword(password);
    await this.submit();
  }

  /**
   * Waits dynamically until the error alert is fully rendered and opaque
   */
  async waitForError() {
    await this.errorAlert.waitFor({ state: 'visible' });
    await expect(this.errorAlert).toHaveCSS('opacity', '1');
    await this.page.evaluate(() => Promise.all(document.getAnimations().map(a => a.finished)));
  }
}
