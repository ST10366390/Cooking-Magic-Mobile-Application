CookMagic - Recipe Management App

Overview

CookMagic is a comprehensive Android cooking application that allows users to browse, search, create, and manage recipes. The app features user authentication, recipe categorization, and a user-friendly interface for all cooking enthusiasts.

Features
User Authentication

• User registration and login
• Profile management with Firebase Firestore
• Secure logout functionality

Recipe Management

• Browse Recipes: View recipes in categorized tabs
• Search Functionality: Search recipes by title, ingredients, or description
• Create Recipes: Add new recipes with images, ingredients, and steps
• Edit Recipes: Modify existing recipes
• Delete Recipes: Remove unwanted recipes
• Recipe Details: View comprehensive recipe information

Category System

• Dynamic category management
• Add new categories
• Filter recipes by categories
• Pre-defined categories: All, Most Viewed, Most Downloaded

Advanced Features

• Real-time search with text filtering
• Image loading with Glide
• Full-screen image viewing
• Recipe sorting options
• User-specific recipe management

Technical Architecture
Frontend

Language: Kotlin
Architecture: MVVM (Model-View-ViewModel)

UI Components:
• Activities
• Fragments
• RecyclerViews with custom adapters
• ViewPager with tabs
• Custom filters

Backend Services

Authentication: Firebase Authentication
Database: Firebase Realtime Database & Firestore
Storage: Firebase Storage for images
Real-time Updates: Firebase listeners

Key Components
Activities

WelcomeActivity - Landing screen
LoginActivity - User authentication
ProfileActivity - User profile management
DashUserActivity - User dashboard with recipe categories
RecipeActivity - Recipe viewing and management
RecipeEditActivity - Recipe creation and editing
CategoryAddActivity - Category management
SearchActivity - Advanced recipe search

Fragments

RecipeUserFragment - Displays recipes by category

Adapters

AdapterCategory - Manages category list display
AdapterRecipeUser - Handles recipe list in user dashboard
SearchAdapter - Manages search results display

Filters

FilterCategory - Implements category search filtering
FilterRecipe - Implements recipe search filtering

Models

ModelUser - User data structure
ModelCategory - Category data structure
ModelRecipe - Recipe data structure

Database Structure
Firestore Collections

users - User profiles and preferences

Realtime Database Nodes

Categories - Recipe categories
Recipes - Recipe data with metadata

Recipe Data Model

{ "id": "unique_recipe_id",
"title": "Recipe Title",
"description": "Recipe description",
"ingredients": "List of ingredients",
"steps": "Cooking instructions",
"img": "Image URL",
"categoryId": "category_reference",
"timestamp": "creation_timestamp",
"uid": "user_id",
"cookingTime": "preparation_time",
"difficulty": "recipe_difficulty",
"servings": "number_of_servings"
}

Installation & Setup
Prerequisites

• Android Studio Arctic Fox or later
• Android SDK 21+
• Google Firebase account

Configuration

Clone the repository
Add your google-services.json file
Configure Firebase projects:
• Authentication (Email/Password)
• Realtime Database
• Firestore Database
• Storage (optional for images)
Build Instructions

Open project in Android Studio
Sync Gradle dependencies
Build and run on emulator or device
Usage Guide
For Users

Registration/Login: Create account or login
Browse Recipes: Navigate through categories
Search: Use search functionality to find specific recipes
View Recipe Details: Tap on any recipe for full details
Create Recipes: Use the add recipe functionality
Manage Profile: Update user information in profile section
For Developers

• The app follows Material Design guidelines
• Uses ViewBinding for type-safe view references
• Implements proper lifecycle management
• Includes error handling and user feedback

Dependencies
Main Libraries

• firebase-auth - User authentication
• firebase-database - Real-time data sync
• firebase-firestore - User data storage
• glide - Image loading and caching
• material-design - UI components

Development Tools

• AndroidX libraries
• Kotlin Coroutines
• ViewBinding

Contributing

Fork the repository
Create feature branch (git checkout -b feature/AmazingFeature)
Commit changes (git commit -m 'Add some AmazingFeature')
Push to branch (git push origin feature/AmazingFeature)
Open a Pull Request
License

This project is licensed under the MIT License - see the LICENSE file for details.

Support

For support and questions, contact the development team or create an issue in the repository.

Version

Current Version: 1.0.0
