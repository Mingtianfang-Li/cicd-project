# 25Spring CS6510 Team 1 - CI/CD System
- **Title:** Tech Stack Design file
- **Date:** Jan 31, 2025
- **Author:** Yiwen Wang, Shenqian Wen
- **Version:** 1.0

**Revision History**
|Date|Version|Description|Author|
|:----:|:----:|:----:|:----:|
|Jan 31, 2025|1.0|Initial release| Yiwen Wang, Shenqian Wen|

# Tech Stack Proposal for CI/CD System

## Deliverables

This document outlines the initial technology choices for developing the CI/CD system, including programming languages, frameworks, libraries, and database implementations. Each choice is justified based on its suitability for the project.

| **Technology**           | **Choice**                      | **Reasoning**                                                |
| ------------------------ | ------------------------------- | ------------------------------------------------------------ |
| **Programming Language** | Java                            | - Java is widely used for backend development in enterprise systems. <br> - Strong ecosystem with mature libraries and frameworks (Spring Boot). <br> - High performance, scalability, and multi-threading capabilities. <br> - Good support for CI/CD pipelineEntity integrations. |
| **Frontend**             | React                           | - React is a modern, component-based frontend framework. <br> - Provides high performance with a virtual DOM. <br> - Well-suited for dynamic dashboards and real-time UI updates. <br> - Rich ecosystem with reusable components and strong community support. |
| **Backend Framework**    | Spring Boot                     | - Provides a powerful and scalable architecture for building web applications. <br> - Supports RESTful APIs, microservices, and security features. <br> - Excellent integration with Java, databases, and messaging queues. <br> - Large community and extensive documentation. |
| **Libraries**            | JUnit, Mockito, Lombok, Jackson | - **JUnit & Mockito**: Essential for unit testing and ensuring code quality. <br> - **Lombok**: Reduces boilerplate code in Java (e.g., getters, setters, constructors). <br> - **Jackson**: Handles JSON serialization/deserialization effectively. |
| **Database**             | MySQL                           | - Relational database with strong ACID compliance. <br> - Well-suited for structured data in CI/CD configurations. <br> - Scalable, reliable, and has good support for transactional consistency. <br> - Broad industry adoption with strong community support. |
| **Message Queue Tool**   | RabbitMQ                        | - Lightweight, high-performance message broker. <br> - Enables asynchronous processing, crucial for CI/CD jobEntity queues. <br> - Supports multiple messaging patterns (publish/subscribe, work queues). <br> - Good integration with Java (Spring Boot) and other services. |
| **CI/CD Tool (Worker)**  | TBD                             | - The choice will be determined based on detailed requirements. <br> - Possible options include Jenkins, GitHub Actions, GitLab CI, or ArgoCD. |

---

## Tech Stack Justification Table (Pros & Cons)

| **Technology**  | **Pros**                                                     | **Cons**                                                     |
| --------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **Java**        | - Strong multi-threading support. <br> - High performance and scalability. <br> - Rich ecosystem and libraries. | - Can be verbose compared to newer languages like Go or Python. <br> - Higher memory consumption. |
| **React**       | - Reusable component architecture. <br> - Strong state management tools. <br> - Fast rendering with Virtual DOM. | - Steep learning curve for beginners. <br> - Frequent updates require maintenance. |
| **Spring Boot** | - Easy to configure and deploy microservices. <br> - Built-in security and dependency management. <br> - Large community support. | - Can have performance overhead compared to lightweight frameworks. |
| **MySQL**       | - ACID compliance ensures data consistency. <br> - Scalable and widely adopted. <br> - Strong SQL query optimization. | - Not ideal for NoSQL-style workloads. <br> - Can have performance issues with very large-scale distributed systems. |
| **RabbitMQ**    | - Efficient messaging for asynchronous task processing. <br> - Supports multiple messaging patterns. <br> - Reliable message delivery. | - Requires additional configuration and monitoring. <br> - Can become a bottleneck under heavy loads if not optimized. |

---

## Next Steps

- Finalize the **CI/CD Tool** based on further project requirements.
- Validate stack choices by creating a **Proof of Concept (PoC)**.
- Ensure seamless **integration** between different components.

---

This document serves as an initial tech stack proposal for developing the CI/CD system.