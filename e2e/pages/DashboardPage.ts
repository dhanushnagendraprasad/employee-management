import { Page, Locator, expect } from '@playwright/test';

export class DashboardPage {
  readonly page: Page;
  readonly heading: Locator;
  readonly loadingSpinner: Locator;
  readonly employeeWrapper: Locator;
  readonly logoutButton: Locator;
  readonly addEmployeeButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.heading = page.getByRole('heading', { name: 'Employee Directory' });
    this.loadingSpinner = page.getByText('Loading data...');
    this.employeeWrapper = page.locator('.employee-wrapper');
    this.logoutButton = page.locator('button:has-text("Logout")');
    this.addEmployeeButton = page.getByRole('button', { name: 'Add' });
  }

  /**
   * Dynamic event-driven wait:
   * 1. Awaits page header
   * 2. Awaits async API loading spinner to disappear
   * 3. Awaits data table wrapper to render
   * 4. Awaits browser rendering pipeline settlement
   */
  async waitForDataLoaded() {
    await this.heading.waitFor({ state: 'visible' });
    await this.loadingSpinner.waitFor({ state: 'hidden' });
    await this.employeeWrapper.waitFor({ state: 'visible' });
    await this.page.evaluate(() => Promise.all(document.getAnimations().map(a => a.finished)));
  }

  /**
   * Clean session termination:
   * Clicks logout and awaits navigation back to the login page.
   */
  async logout() {
    await this.logoutButton.waitFor({ state: 'visible' });
    await this.logoutButton.click();
    await this.page.waitForURL('**/login');
  }
}
